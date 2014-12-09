Bug fixes
=========

The following is a list of outstanding bugs that need fixing.

High priority
-------------
*   Server should serialize/deserialize saved worlds
*   Player states should be saved between logins


Low priority
------------
*   Fix terminal resizing crash
*   Replace lots of string concatenation occurrences with StringBuilder
*   Special replacements need to be optimized
*   Add player status variables
    -   Events might be triggered after ticks (e.g., stomach growls)
    -   Sickness
        *   condition of each major part (skeleton)
        *   blood loss level (?)



Planned features
================
*   Client/server protocol should be developed to allow more intelligent
    clients
    -   Client should be able to understand world parameters like time,
        weather, etc. and other details about locales
    -   If client could download and parse world file, then it could present
        room descriptions faster and more reliably to player
*   Add scenes
*   Server/client should exchange versions before connecting
*   Players should be able to author descriptions of their character
*   "look" action should respond to requests to describe a player in the room
*   Add sentence analysis techniques to choose say-verbs based on vocabulary
    in sentence (e.g., 'think' should choose from 'suggests' or 'surmises')
*   'continues' should be used when a player continues speaking, or the verb
    could be omitted entirely
*   `UseableItem`s should be able to modify the universe and add events
    to the dispatcher
*   There should be pedagogical `UseableItem` implementations like maps,
    compasses, or two-way radios
    -   Clocks that reflect real time (but not necessarily set) and whose
        batteries can die
*   Functionality for hidden exits


Feature wishlist
================
*   Servers should have customizable welcome and farewell messages
