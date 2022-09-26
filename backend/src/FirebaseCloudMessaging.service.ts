const FCM = require("fcm-push")
const SERVER_KEY = "AAAAf_hIErk:APA91bHEPBexIOY5cYnD6fXs5Y0EzntuKn8binlcdvTrGUG9JZ430haWL4RaqpiS-MH0f1TqTtwtM5wpbcwm5FvT4m0QzIXwmwBwhDo81chGdtgNRmSclj-AWWIRnlQM0bcL4dxFuG03"

export class FirebaseCloudMessaging {
	  private client
	  private static inst: FirebaseCloudMessaging
	  
	  static getInstance() {
		  if(FirebaseCloudMessaging.inst === undefined) {
			FirebaseCloudMessaging.inst = new FirebaseCloudMessaging()
		  }
		  
		  return FirebaseCloudMessaging.inst
	  }
	  
	  constructor() {
		this.client = new FCM(SERVER_KEY)
	  }
	  
	  sendMessage() {
		  const message = {
			  to: "/topics/spots",
			  collapse_key: "collapse_key",
			  notification: {
				  title: "New Spot has been created!",
				  body: "Hey a new spot has been just registered. Want to check it out on the map?",
			  },
			  android: {
				  ttl: "60s",
			  },
			  data: {
				  "channel":"HEADS_UP_NOTIFICATION"
			  }
		  }
		  
		  this.client.send(message, (err: never, response: never) => {
			  if(err) {
				  console.log("Something went wrong!", err)
			  } else {
				  console.log("Sent notification!", response)
			  }
		  })
	  }
	  
	  sendMessageVisited(personalToken: string, spot:string, spot_id:number) {
		  const message = {
			  token: personalToken,
			  collapse_key: "collapse_key",
			  notification: {
				  title: "Are you at" + spot + "?",
				  body: "Looks like you have been a while at " + spot + ". Do you want to add it to your visit history?",
			  },
			  android: {
				  ttl: "60s",
			  },
			  data: {
				  "channel":"SPOT_VISITED_CHANNEL",
				  "id": + spot_id,
			  }
		  }
		  
		  this.client.send(message, (err: never, response: never) => {
			  if(err) {
				  console.log("Something went wrong!", err)
			  } else {
				  console.log("Sent notification!", response)
			  }
		  })
	  }
  }