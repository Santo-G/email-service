# Java email service
The project's aim is to provide a scalable, parallelized and distributed Java application that implements an email service with a GUI.
The source code contains both server and client implementations, uses MVC pattern and follows Observer pattern principles.

## Table of Contents
- [Project description](#project-description)
- [Technical requirements](#technical-requirements)
- [GUI requirements](#gui-requirements)
- [Notes](#notes)
- [Technologies](#technologies)
- [Tools](#tools)
- [Contributing](#contributing)

## Project description 
The project is structured with an email server, that handles both the user’s mailboxes and the necessary mail clients, that allows users to log into their respective mailboxes.
- Mail server handles a list of email boxes and maintains their persistency using a __file__ (.txt or binary – no database) to memorize messages permanently
- Mail server is provided with a GUI that shows the logs of all __actions__, done by the mail clients, and all __events__ occurred during interaction between clients and server
  * E.g.: opening/closing connection between mail client and server, sending messages, receiving messages, errors for delivering messages
  * Logs for local events are not included in this list (e.g. button pressed, window opened etc.)
- A single mail box contains:
  * __Name__ of the mail account associated with the mail box (e.g. george[at]mymail.com)
  * __List of messages__ (eventually empty). Messages are instances of a class named Email that specify ID, sender, receiver/s, object, message body and sending date
- The mail client associated to a single mailbox is provided with a GUI described as follows. 
  * GUI allows to:
  * Create and send a message to one or more recipients (multiple recipients for a single message sent)
  * Read all messages of the mailbox
  * Reply to a received message: Reply (only to the sender), Reply-all (to the sender and all recipients that received the message)
  * Forward a message to one or more email accounts
  * Delete a message from mailbox
  - GUI always shows the updated list of messages received in mailbox and, when a new message arrive, send a notification to the user through a dialog window
  - Registration and login functionalities, even in the simplest way possible, are required
  
## Technical requirements
Mail clients should not crash if the server is switched off. Manage all possible connection problems, e.g. to the email server, sending appropriate messages to the user.
- The project should work at least with __3 email clients__ that sends messages each other. The project should be __scalable__ for more than 3 users
- The application has to be developed using __Java__ language and __MVC pattern__ following basic principles of __Observer pattern__. Direct communication between View and Model is not allowed; every type of communication between these two layers has to be managed by the Controller
- The application must allow to correct any wrong inputs from the users (e.g. for nonexistent email addresses server has to send an error message to the sender; in case of syntactically wrong email address the client itself has to notify user without trying to send the message to the server)
- Client and server has to __parallelize all the activities__ that does not needs sequential execution, managing any mutual access problem to shared resources. Client and server has to be separated Java applications; creation e management of messages has to be parallelized to the receipt of new messages
- The application has to be __distributed__ (email clients and server has to be on distinct JVM) using __Java Sockets__

## GUI requirements
- GUI has to be:
  * __Understandable__. E.g. for errors it has to notify the type of problem to the user
  * __Reasonably efficient__ to let the user to do basic operations with the minimum amount of click and data entry
  * Implemented using __Java FXML__ and, if necessary, __Threads__. Use of Java Beans, properties and binding properties is not required

## Notes
It is recommended to pay attention to the design phase to obtain the easiest implementation of parallelism during the execution and distribution on distinct JVMs.

## Technologies
- Java SE 14
- Java FXML

## Tools
- IntelliJ IDEA Ultimate 
- Scene Builder
- GitHub Actions

## Contributing
<a href="https://github.com/LucreziaC">
 <img alt="LucreziaC" width="90" height="90" src="https://avatars.githubusercontent.com/u/37901017?v=4" />
</a>

<a href="https://github.com/Santo-G">
 <img alt="Santo-G" width="90" height="90" src="https://avatars.githubusercontent.com/u/77076220?v=4" />
</a>

