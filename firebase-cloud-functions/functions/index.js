const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { database } = require('firebase-admin');
const { ref } = require('firebase-functions/lib/providers/database');
admin.initializeApp();

// Cut off time. Child nodes older than this will be deleted.
const CUT_OFF_TIME = 1000 * 60 * 60 * 24 * 7 * 30; // 30 days in milliseconds.

const deleteUserAppointments = (user) => {
    const ref = database().ref();
    const updates = {}
    const userAppointmentsQuery = ref.child('/appointments').orderByChild('clientUid').equalTo(user.uid).once('value')
        .then(appointmentsSnapshot => {
            appointmentsSnapshot.forEach(child => {
                updates[`/appointments/${child.key}`] = null;
            })
            return ref.update(updates)
        })
}

const deleteLatestMessages = (user) => {
    const ref = database().ref();
    const updates = {}
    const latestMessagesQuery = ref.child('/contacts-latest-messages').once('value')
        .then(latestMessagesSnapshot => {
            latestMessagesSnapshot.forEach(child => {
                child.forEach(child2 => {
                    if (child2.key === user.uid) {
                        updates[`/contacts-latest-messages/${child.key}/${child2.key}`] = null
                    }
                })
            })
            return ref.update(updates)
        })
}

const deleteMessages = (user) => {
    const ref = database().ref();
    const updates = {}
    const messagesQuery = ref.child('/messages').once('value')
        .then(messagesSnapshot => {
                messagesSnapshot.forEach(child => {
                    child.forEach(child2 => {
                        if (child2.key === user.uid) {
                            updates[`/messages/${child.key}/${child2.key}`] = null
                        }
                    })
                })
                return ref.update(updates)
            })
}

exports.deleteUser = functions.auth.user().onDelete((user) => {
    const ref = database().ref();
    const updates = {};
    updates[`/userData/${user.uid}`] = null;
    updates[`/messages/${user.uid}`] = null;
    updates[`/contacts-latest-messages/${user.uid}`] = null;
    updates[`/admin/messages/${user.uid}`] = null;
    updates[`/admin/latest-messages/${user.uid}`] = null;

    deleteUserAppointments(user)
    deleteLatestMessages(user)
    deleteMessages(user)

    return ref.update(updates);
});

/**
 * Triggers when a user gets a message and sends a notification.
 *
 * Messages add a flag to `/messages/{receiverUid}/{senderUid}`.
 * Users save their device notification tokens to `/userData/${uid}/notificationTokens/{notificationToken}`.
 */
exports.sendMessageNotification = functions.database.ref('/messages/{receiverUid}/{senderUid}/{chatMessage}')
    .onCreate(async (snapshot, context) => {
      const receiverUid = context.params.receiverUid;
      const senderUid = context.params.senderUid;
      /*
      // If un-follow we exit the function.
      if (!change.after.val()) {
        return console.log('User ', followerUid, 'un-followed user', followedUid);
      }*/

      // If sender is current user
      if (receiverUid === snapshot.val().fromId) {
        return console.log("Sender was current user");
      }
      
      // Get the list of device notification tokens.
      const getDeviceTokensPromise = admin.database()
          .ref(`/userData/${receiverUid}/notification-tokens`).once('value');

      // Get the sender profile
      const getSenderProfilePromise = admin.database()
        .ref(`/userData/${senderUid}`).once('value');

      // The snapshot to the user's tokens.
      let tokensSnapshot;

      // The array containing all the user's tokens.
      let tokens;

      const results = await Promise.all([getDeviceTokensPromise, getSenderProfilePromise]);
      tokensSnapshot = results[0];
      const sender = results[1];

      // Check if there are any device tokens.
      if (!tokensSnapshot.hasChildren()) {
        return console.log('There are no notification tokens to send to.');
      }
      console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
      console.log('Fetched sender profile', sender);

      let title;
      if (sender.userType === "Client") {
        title = "New Message";
      } else {
        title = `New Message from ${sender.val().name}`;
      }

      // Notification details.
      const payload = {
        notification: {
          title: title,
          body: `${snapshot.val().text}`
        }
      };

      // Listing all tokens as an array.
      tokens = Object.keys(tokensSnapshot.val());
      // Send notifications to all tokens.
      const response = await admin.messaging().sendToDevice(tokens, payload);
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
});

/**
 * Triggers when a message is sent in admin chat and sends a notification to all admins or to user.
 *
 * Admin messages add a flag to `/admin/messages/{senderUid}`.
 * Admin notification tokens are saved to `/notification-tokens/admin/{notificationToken}`.
 */
exports.sendAdminMessageNotification = functions.database.ref('/admin/messages/{senderUid}/{chatMessage}')
    .onCreate(async (snapshot, context) => {
      const senderUid = context.params.senderUid;

      // The snapshot to the tokens.
      let tokensSnapshot;

      // The array containing all the tokens.
      let tokens;

      // Notification details.
      let payload;

      // If sender is admin
      if (snapshot.val().fromId === 'admin') {
        // Get the list of device notification tokens.
        const getDeviceTokensPromise = admin.database()
            .ref(`/userData/${senderUid}/notification-tokens`).once('value');

        const results = await Promise.all([getDeviceTokensPromise]);
        tokensSnapshot = results[0];

        // Check if there are any device tokens.
        if (!tokensSnapshot.hasChildren()) {
            return console.log('There are no notification tokens to send to.');
        }

        payload = {
            notification: {
            title: `Message from Admin`,
            body: `${snapshot.val().text}`
            }
        };

      } else {
      
        // Get the list of device notification tokens.
        const getDeviceTokensPromise = admin.database()
            .ref(`/notification-tokens/admin`).once('value');

        // Get the sender profile
        const getSenderProfilePromise = admin.database()
            .ref(`/userData/${senderUid}`).once('value');

        const results = await Promise.all([getDeviceTokensPromise, getSenderProfilePromise]);
        tokensSnapshot = results[0];
        const sender = results[1];

        // Check if there are any device tokens.
        if (!tokensSnapshot.hasChildren()) {
            return console.log('There are no notification tokens to send to.');
        }

        payload = {
            notification: {
                title: `Message to Admin from ${sender.val().name}`,
                body: `${snapshot.val().text}`
            }
        };
      }

      // Listing all tokens as an array.
      tokens = Object.keys(tokensSnapshot.val());
      // Send notifications to all tokens.
      const response = await admin.messaging().sendToDevice(tokens, payload);
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
});

/**
 * Triggers when an appointment is made and sends a notification to all admins and service providers.
 *
 * Appointments add a flag to `/appointments/{appointment}/`.
 * Admin notification tokens are saved to `/notification-tokens/admin/{notificationToken}`.
 * Service provider notification tokens are saved to `/notification-tokens/service-providers/{notificationToken}`.
 */
exports.sendBookAppointmentNotification = functions.database.ref('/appointments/{appointment}')
    .onCreate(async (snapshot, context) => {
      
      // Get the list of device notification tokens.
      const getAdminDeviceTokensPromise = admin.database()
          .ref('/notification-tokens/admin').once('value');

      const getServiceProviderDeviceTokensPromise = admin.database()
        .ref('/notification-tokens/service-providers').once('value');

      const getUserDeviceTokensPromise = admin.database()
        .ref(`/userData/${snapshot.val().clientUid}/notification-tokens`).once('value');

      // The snapshot to the tokens.
      let tokensSnapshotAdmin;
      let tokensSnapshotServiceProvider;
      let tokensSnapshotUser;

      // The array containing all the tokens.
      let tokens;

      const results = await Promise.all([getAdminDeviceTokensPromise, getServiceProviderDeviceTokensPromise, getUserDeviceTokensPromise]);
      tokensSnapshotAdmin = results[0];
      tokensSnapshotServiceProvider = results[1];
      tokensSnapshotUser = results[2];

      // Check if there are any device tokens.
      if (!tokensSnapshotAdmin.hasChildren() && !tokensSnapshotServiceProvider.hasChildren() && !tokensSnapshotUser.hasChildren()) {
        return console.log('There are no notification tokens to send to.');
      }

      // Notification details.
      const payload = {
        notification: {
          title: "New Appointment Booked",
          body: `${new Date(snapshot.val().timestamp * 1000)}`
        }
      };

      // Listing all tokens as an array.
      tokens = Object.keys(tokensSnapshotAdmin.val()).concat(Object.keys(tokensSnapshotServiceProvider.val())).concat(Object.keys(tokensSnapshotUser.val()));
      // Send notifications to all tokens.
      const response = await admin.messaging().sendToDevice(tokens, payload);
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
});

/**
 * Triggers when an appointment is made and sends a notification to all admins and service providers.
 *
 * Appointments add a flag to `/appointments/{appointment}/`.
 * Admin notification tokens are saved to `/notification-tokens/admin/{notificationToken}`.
 * Service provider notification tokens are saved to `/notification-tokens/service-providers/{notificationToken}`.
 */
exports.sendCancelAppointmentNotification = functions.database.ref('/appointments/{appointment}')
    .onDelete(async (snapshot, context) => {
      
      // Get the list of device notification tokens.
      const getAdminDeviceTokensPromise = admin.database()
          .ref('/notification-tokens/admin').once('value');

      const getServiceProviderDeviceTokensPromise = admin.database()
        .ref('/notification-tokens/service-providers').once('value');

      const getUserDeviceTokensPromise = admin.database()
        .ref(`/userData/${snapshot.val().clientUid}/notification-tokens`).once('value');

      // The snapshot to the tokens.
      let tokensSnapshotAdmin;
      let tokensSnapshotServiceProvider;
      let tokensSnapshotUser;

      // The array containing all the tokens.
      let tokens;

      const results = await Promise.all([getAdminDeviceTokensPromise, getServiceProviderDeviceTokensPromise, getUserDeviceTokensPromise]);
      tokensSnapshotAdmin = results[0];
      tokensSnapshotServiceProvider = results[1];
      tokensSnapshotUser = results[2];

      // Check if there are any device tokens.
      if (!tokensSnapshotAdmin.hasChildren() && !tokensSnapshotServiceProvider.hasChildren() && !tokensSnapshotUser.hasChildren()) {
        return console.log('There are no notification tokens to send to.');
      }

      // Notification details.
      const payload = {
        notification: {
          title: "Appointment Canceled",
          body: `${new Date(snapshot.val().timestamp * 1000)}`
        }
      };

      // Listing all tokens as an array.
      tokens = Object.keys(tokensSnapshotAdmin.val()).concat(Object.keys(tokensSnapshotServiceProvider.val())).concat(Object.keys(tokensSnapshotUser.val()));
      // Send notifications to all tokens.
      const response = await admin.messaging().sendToDevice(tokens, payload);
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
});

