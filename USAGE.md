# In-game actions

The following are actions available for a player to perform. The angled
brackets < and > indicate an object of the specified type that should be
replaced when in actual use. For example, "take <object>" suggests that a
player may pick up a bottle by typing "take", a space, and then the
object's name: "take bottle". Similarly, "move <direction>" suggests that
"move north", "move right", or "move down" are all acceptable. Square
brackets [ and ] indicate that the presence of the contained object is
optional. A list enclosed by curly brackets { and } indicate that only one
item from its comma-separated list can be chosen.

    ACTION                    OBJECT        INDIRECT OBJECT
    [{m,move,go,walk}]        <direction>
    {t,take,get}              <object>
    {g,give}                  <object>      to <player>
    {l,look,describe}         [<object>]
    {i,inventory}
    {e,exits}
    {s,say,talk}              [<string>]
    {y,yell,shout}            <string>
    {w,whisper}               <string>      to <player>
    {u,use}                   <object in inventory>
    {h,help}
    {q,quit}

Wherever applicable, the pseudo-object "here" can be used. The default
behavior of "look" without an object is equivalent to "look here". The
default behavior of "say" without an object will cause the narration
"<player> remains silent." to be written.

The "yell" action causes the player to shout very loud, so that players
may hear from several rooms away. Whispering to a player is only possible
when they are in the same room. Communicating with a player across a vast
distance is as difficult here as it is in real life, though objects in the
game such as flares, signs, letters and locked boxes are useful
augmentations of communication.
