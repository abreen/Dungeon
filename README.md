# Dungeon

`Dungeon` is a multiplayer text adventure game engine written in Java. It
is inspired by traditional text adventure games from years ago, with some
new ideas and features.

This project comes with a server and client. See the `USAGE.md` file for
more information about how to use them.

`Dungeon` is my first exploration into text-based games and network 
programming. You're welcome to help out with development.

There is no "license" for the source code, apart from the requirement that
you don't sue me if it breaks anything. Expect me to find an appropriate
license in due time.


## Extensibility

At the moment, the server automatically constructs a boring universe for
players to roam around in. I am in the process of building in a YAML
parser so people can write their own game universes and load them into the
server. This is what makes `Dungeon` a bit more flexible than any old text
adventure game.

I'll get around to authoring a small universe demoing the features that
`Dungeon` has to offer.


## Dependencies

The server uses [SnakeYAML][snakeyaml], a YAML parser for Java. The JAR
file (version 1.12) is included in the `lib` directory.

The client uses [Lanterna 3][lanterna], a pure Java terminal UI library
that's super cool, and is included in the `lib` directory.

I've been using some Java 1.7-only syntax, so you'll need (at least)
Java 1.7.


## Building and running

See `USAGE.md`.

[snakeyaml]: http://code.google.com/p/snakeyaml/
[lanterna]: http://code.google.com/p/lanterna/
