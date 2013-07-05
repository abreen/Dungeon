Bug fixes
=========
The following is a list of outstanding bugs that need fixing.

High priority
-------------

* Fully implement actions by implementing and using new concurrent methods
  in `DungeonUniverse`
* The client is not functional


Low priority
------------

* The `toString` method in the `Item` class should concatenate article
  and noun



Planned features
================
* `UseableItem`s should be able to modify the universe and add events
  to the dispatcher
* There should be pedagogical `UseableItem` implementations like maps,
  compasses, or two-way radios


Feature wishlist
================
* Servers should have customizable welcome and farewell messages
* Servers should have a timer to support time-based actions
  - In-game weather
  - In-game day and night
* Servers should send text formatting codes to the client
