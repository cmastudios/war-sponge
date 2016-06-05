Zone Maker Guide
=====

Builders and technicians unite! In comparison to its predecessor, War 2.0 offers greater
customization possibilities in all aspects of zone creation.

Creating a Warzone
-----

You may choose to either build the entire zone and then load it into War, or set the zone
boundaries and then begin creation.

To begin creation, you must find two corners of the warzone that encompass all the blocks
and air space.

![Cuboid Diagram](http://wiki.sk89q.com/w/content/d/dc/Cuboid.png)

Start by running `/setzone name`, where `name` is the name of the zone you wish to create.
Don't have any ideas for zone name? Try ziggy, valley, or oasis.

Then, find the first corner of the warzone, point your cursor directly at the block, and
run `/setzone c1`. This will register the first corner of the warzone boundary. Then, find
the second corner, point your cursor, and run `/setzone c2`. Voila! The warzone is now
registered in the plugin, and a data file has been created. **The blocks in the warzone
have not yet been saved!**

Saving Warzone Blocks
-----

Once the warzone has been set up, run the command `/savezone name` to save the blocks in
the warzone identified by `name`. *This will overwrite the last saved block information.*
To keep versions of warzones, the server administrator must run backups of the warzone
data files. Since all data is overwritten, there is no harm in saving the zone several
times during build. Make sure to not save zone data while a game is active, in case
players have damaged the state of the zone, unless you do this intentionally.

Reloading Warzone Blocks
-----

The command `/resetzone name` will reload all the blocks in warzone `name` to those last
saved. If a game is active, this will end the round and reset the blocks.

Changing Warzone Configuration
-----

`/zonecfg <zone> <option> <value>` - This command changes a particular setting for one
warzone.

With no parameters, it lists all available warzones and properties. With one parameter,
it lists the zone's current settings. With two parameters, it shows the value of setting
*option* in warzone *zone*. With three parameters, it sets the value of the particular
setting.

### Available options

- `editing` - if set to `true`, it will prevent the creation of new games in the zone and
    it will allow zone makers to enter the zone and make changes.
- `maxplayers` - limits the number of players who may play in a game in the zone.
- `maxpoints` - after a team has obtained this number of points, a new round will commence.
- `maxrounds` - limits the number of rounds that may be played in any one game. Once this
    number has been reached, all players will be removed from the zone and sent to the
    lobby. Useful to encourage players to play other zones or take breaks.

Setting Locations
-----

`/location <zone> <type> [team] [-delete]` - This command sets a particular location for a
zone.

Location types: lobby, spawn, or gate.

The lobby location is where players will spawn when they teleport to the warzone. This is usually a platform
overlooking the zone with gates to join teams.

To create a team in the zone, set the location for their spawn. Players will teleport to the
    team spawn location when they die.

Gates teleport players to teams, and therefore take the team name as a parameter. If the
team name is set to `autoassign`, players will be randomly assigned to teams.

Using -delete after a spawn or gate will delete the team or the gate respectively.

Deleting Zones
-----

Maybe the warzone has just passed its peak. Maybe it was an early prototype. Maybe even
a server allows the public to create warzones, and some are sub-par. There are many reasons
why one must eventually delete a warzone. To unload a warzone from the server, use
`/deletezone name`. Blocks in the warzone will be unaffected (reload them first!). The
warzone data file will be moved to a trash folder on the server.