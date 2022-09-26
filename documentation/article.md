# Article

This article is written as an additional overview of the mobile application developed by Android group 6 in PRJ4. 
The purpose of the article is to inform the assessment committee about the overall architecture of the software system, 
as well as to explain which design patterns and principles have been applied and how and why they have been integrated. 
The article will also provide individual explanation of the contribution to the project written by each team member.

## Overview
The application developed by our group is called 'Vista' as it encourages people to "visit" different interesting and cool places.
The key goal of our system is to provide a space for android mobile users to share details and photos about interesting places that can be 
visited for a nice sight. The system allows users to easily find nearby spots and overview them, rate them, update them with new photos or even
to create a completely new spot on the go. Administrators and privileged users also have the rights to edit and delete spots as well as to ban and unban users.
With our app we aim to bring people closer, to let them easily explore, share and enjoy the world around them anytime, anywhere.

## Overall architecture 
In terms of architecture our application has three major components - the mysql database, the loopback backend rest API and the frontend Android client app.

For the database we are using MYSQL Linux ubuntu server 8.0.24 that is hosted on a virtual machine service center. The connection to the database layer
is done via Loopback as we made a connector for a database source and we linked our existing database to our loopback project. 

The loopback project consists of the database connector, data models for our different entities as well as CRUD repositories for them linked to the 
MYSQL database connector. The functionality is written down in the controllers, where the usage of the operations of the repositories,
as well as the actual logic lies. We don't have to worry about schema design as Loopback automatically migrates the schema to our MYSQL database. 

Our frontend applications consists of a persistence layer used for sending HTTP requests to our backend API via the java.net library. 
It is also used for parsing the response received by the server. Other than the persistence layer we also have a business logic and GUI layers
of our app. The GUI encapsulates the XML design components, which are used for visualizing our different pages. The business logic includes foreground and
background tasks. The foreground tasks are controlling the different android activities and fragments, where as the background task
is just keeping track of the user locations and also triggers some mobile notifications based on that location.

## Individual roles
In this part we will individually describe our role in the team during this project as well as our issues, solutions and achievements.

### Alex
As a team member I worked mostly on the frontend. I helped to work on the design of the app, for example before starting to code I helped on creating our first UI designs on 'Figma'. I also worked in peer programming together with Kristijan on the design and implementation of the profile overview page, which shows the overall information about a user and also gives the opportunity to log out. The information on this page includes a list of all spots visited that needs to be pulled from the backend, showing their name, ID, and the time and date when they were visited, as well as a list of the spots added by the person who is currently logged in, including the creating date and time, the name, and their ID. I also helped Kristijan and Daniel with the spot overview. 

Therefore, I gained a lot of experience with how to work with the various elements, such as buttons and especially the list view, which was difficult to implement to our desire as it took some time to figure out how to perfectly display the data in an organized manner. Also, I learned how to work with the REST API endpoints that were mainly worked on by Yorick and Daniel. 

I also gave feedback and helped with problem solving with other teammates and actively participated in our stand-up meetings, retrospectives, and weekly sprint reviews and also in the discussions and planning that resulted in our explicit app idea in form of the use cases and requirements document. Additionally, I worked on the editing of the video presentation deliverable.

### Benjamin
As a member of the development team, I worked most of the time on the frontend of our app. 
This included the process of planning, structuring, and designing the overall theme of the application. 

The outline of the app was sketched in Figma, where the team had access to a shared file to collaborate
on the same project.
Building the frontend included working closely with the Android Studio IDE. I build different XML 
layouts, consisting of different components and views to display as the GUI on the phone screen of 
the end user. To add life to the design I created activities and fragments, to handle all the logic 
that happens in the view. Most of the time I worked on the Login-, Register-Activity as well as the 
Maps- and “AddSpot”-Fragment with Rachel.
In addition to that I also enabled traversing through the application by adding a bottom navigation 
bar to the bottom of the app, which would host all the needed fragments in a host view.

Furthermore, I decided on a theme, by defining some static colors to use throughout the whole project 
as well as certain font families. By adding and designing some icons and background SVGs, Rachel and 
I gave the app our own personal touch.

When working with Google Maps API as well as the user’s phone camera, I managed to provide the outline
of the process of handling permissions in Android. With said logic we were able to implement functions
like tracking the user’s location or letting them take picture with their phones to upload to our backend.

Eventually, I connected Firebase with our Android Studio project, which made it possible to let Yorick
implement the push notification services.

As an agile team member, besides working on the Android project itself, I also participated in weekly
sprint reviews as well as the retrospective and stand-up meetings. Like any other member I was also in
charge of maintaining our scrum board on Jira by assigning myself to various tasks as well as moving,
estimating, and grouping them under epics. With the other team members, I helped the scrum master to
write down the weekly retrospective notes to hand in as a weekly deliverable.


### Daniel
As a scrum master I facilitated the work in my team. I was making sure that our artifacts and documentation is up to date. I was making sure
that all team members are included in the work process and are not left unheard or ignored by the rest of the team. I was timeboxing our meetings,
as well as trying to keep the discussion in scope of the meeting. I was writing down our sprint result documentation and making sure everything is 
uploaded to canvas, github, jira, feedpulse, etc. I was also taking the cockpit for my group so we can collaborate together in a quiet environment 
and making sure that all team members can easily find the group when meeting is on campus.

As a team member I worked more on the backend than I did on the frontend. I together with Yorick wrote the whole Loopback project - I generated some of the 
data models such as user, spot, spot visit, etc. I also generated the CRUD repositories for those data models and linked them to the MYSQL database connector.
I then wrote some of the controllers such as the spot controller, spot visited controller, weather controller, etc. I also helped Yorick with the writing 
of the persistence layer as we applied the principle of peer-programming to avoid making mistakes in the persistence layer as it is vital for out application.
I also worked on some of the Android functionality - mainly one the Spot overview page, where a spot can be viewed, edited, deleted, rated or updated with pictures.
I also assisted my team members with the Profile overview page and the Map fragment where I was mainly working on making the API calls to our backend API and 
parsing and linking the response data to the appropriate Android component. I also did some minor code cleanup and helped with the design of some of the pages.
 

### Kristijan
As a team member I mostly worked on the frontend and participated in the weekly sprint reviews, the retrospective and stand-up meeting. Further I did my share on the Jira scrum board. Here I assigned myself some tasks, estimated them and moved them to various epics.
In the beginning of the project, I worked on the planning, structuring, and designing of the overall app together with Rachel, Benjamin, and Alex. For that we made some sketches in Figma, where the whole team could add notes or change specific parts of the design. 
Later those sketches were implemented in Android Studio. I started by drafting general activities and their design implementations with their respective xml’s for the registration and login as well as some drawable xml’s for a consistent design in terms of text, buttons, and background. After that I worked on an overview for the spots, which was later reworked a lot in its functionality by Daniel. While I was working on that, Benjamin and I figured out how to access fragments on top of activities via the navigation bar. 
Lastly, the profile was implemented by me and Alex. Here we got some help from Daniel for working with the REST API endpoints. As there was some out-of-scope content, we decided to only add two lists. One contains information about added spots by the user and the other shows visited spots by the user. 


### Rachel
As a product owner I was in charge of making sure that the overall goal at the end of each sprint was achieved. For this I checked the overall artifacts that resulted from that particular sprint at every sprint review meeting. It is important to make sure that the product backlog was always filled and updated based on the changing goal.<br>
One of the reasons we had to imitate a product owner while also being part of the development team is that we should learn the agile driven development process, which is used in many organizations in real life.

As a development team member I mainly worked in the frontend. Here I helped to design wire frames in Figma for the different activities so that further implementation would stay in a consistent look. <br>
Once the design was in place the implementation of the frontend layouts were done as a XML files in Android Studio IDE. For this task I worked together with Benjamin to achieve the consistent look and tackle the issues that were encountered from using Android Studio for the first time.<br>
After the layout for the fragment was done it needed to receive life to actually use the buttons and so forth. Here I worked mainly on the ‘Add new Spot’ and ‘Register’ fragments. This resulted that the user could actually register or add a new spot with all the functionality working. Here I also asked Yorick for help on how to use the backend from the frontend and we pair programmed that bit together. Together with Benjamin all issues were solved for these fragments and the tasks from each fragment were divided between us.


### Yorick
As a team member I worked in the start of the project mainly on the backend. This is because of my prior bad experience with android studio. Because of this I wanted to focus on the backend. Mainly to note for the backend is that we did not use the standard generate options from loopback. This is because these files become very messy very quickly. Because of that we decided to wirte all the CRUD operations by hand, keep the files & explorer neatly clean with also only endpoints used by our application. It gave some complications regarding the authentication systems with the tokens. In the end we got it all working!

Later on in the project once backend was very much completed and the basis front end was constructed I also helped out in the front end. I dubbeged for exmaple the application for the demo. Helped linking the backend with front end, wrote the presistance layer in style of pair-programming. 

Also in the end I helped with dotting the i's in the front end and fixing major bugs but also smaller bugs. Because of this I touched every part of the front end atleast a bit. I also worked together with other team members on their fragments to make sure everything links correctly and was working correctly. In the end after firebase was setup I made sure our push notifications where working even when the app is in the background. 
