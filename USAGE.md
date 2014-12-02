# Usage

## Starting the server

First, invoke `make` to compile the Java sources. Then use the `run`
shell script as follows:

    ./run server

The server should start up on port 5554.

## Starting the client

Assuming `make` has already been invoked, use the `run` shell
script as follows:

    ./run client

The client should print a usage message. Follow its instructions to
specify the server to connect to.


## Playing the game

The following are actions available for a player to perform. The angled
brackets < and > indicate an object of the specified type that should be
replaced when in actual use. For example, `take <object>` suggests that a
player may pick up a bottle by typing `take`, a space, and then the
object's name: `take bottle`. Similarly, `move <direction>` suggests that
`move north`, `move east`, or `move down` are all acceptable. Square
brackets [ and ] indicate that the presence of the contained object is
optional. A list enclosed by curly brackets { and } indicate that only one
item from its comma-separated list can be chosen.

    ACTION                    OBJECT        INDIRECT OBJECT
    [{m,move,go,walk}]        <direction>
    {t,take,get}              <object>
    {d,drop}
    {g,give}                  <object>      to <player>
    {l,look,describe}         [<object>]
    {i,inventory}
    {e,exits}
    {s,say,talk}              [<string>]
    {y,yell,shout}            <string>
    {w,whisper}               <string>      to <player>
    {u,use}                   <object in inventory>

Wherever applicable, the pseudo-object "here" can be used. The default
behavior of `look` without an object is equivalent to `look here`. The
default behavior of `say` without an object will cause the narration
"<player> remains silent." (or similar) to be written.

The `yell` action causes the player to shout very loud, so that players
may hear from several rooms away. Whispering to a player is only possible
when they are in the same room. Communicating with a player across a vast
distance is as difficult here as it is in real life, though objects in the
game such as flares, signs, letters and locked boxes are useful
augmentations of communication.

The following so-called server actions allow the player to get information
about the server that is simulating the current universe.

    SERVER ACTION
    help
    who                       [<player>]
    quit

The `who` action prints a list of all the players currently online.
The `help` action prints an abbreviated version of this usage document.
