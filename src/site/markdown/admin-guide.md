Admin Guide
===========

Installing War
-----

Use one of the following links to download the plugin for your server software:

- [Download latest War for Bukkit (2.0 preview)](http://ci.tommytony.com/job/War-Sponge/com.tommytony$war-bukkit/lastSuccessfulBuild/artifact/com.tommytony/war-bukkit/2.0-SNAPSHOT/war-bukkit-2.0-SNAPSHOT.jar)
- [Download latest War for Sponge (2.0 preview)](http://ci.tommytony.com/job/War-Sponge/com.tommytony$war-sponge/lastSuccessfulBuild/artifact/com.tommytony/war-sponge/2.0-SNAPSHOT/war-sponge-2.0-SNAPSHOT.jar)

Configuration
-----

War 2.0 has no user-editable configuration files; all configuration must be
performed from within the game through either the `/warcfg` command or the `/zonecfg` command.

### `/warcfg [setting] [value]`

This administrator-only command allows one to edit the settings for the plugin
and the default settings for future warzones.
With no arguments, it lists all War settings and zone defaults with their current value.
With one argument, it displays the value of the War setting or zone default.
With two arguments, it updates the value of the War setting or zone default to the provided argument.

Try running `/warcfg` on your server to list the available properties. Notice
the lack of extensive configuration. Most of the options are the responsibility of
your zone makers.

Permissions
-----

- `war.teleport` - Gives the user the permission to teleport to warzones, with the
`/zone` command. On Bukkit, this is granted by default.

- `war.zonemaker` - Gives the user the permission to create, edit, and delete warzones. This
privilege can be further fine-tuned by granting instead several of the more specific permissions
below:

    - `war.zone.create` - Ability to set warzones
    - `war.zone.delete` - Ability to delete warzones
    - `war.zone.config` - Ability to change warzone configuration
    - `war.zone.save` - Ability to save warzone blocks
    - `war.zone.reset` - Ability to reload warzone blocks
    - `war.zone.construct` - Ability to build in a zone while disabled

- `war.config` - Gives permission to edit the global War config. Administrator-only privilege.

The plethora of permissions gives the administrator the possibility to adapt to specific
scenarios. For example, zone makers on a public server may use their power to delete
other desired warzones. To solve such an issue, one may only grant specific zone privileges to
zone makers instead of the blanket list.

Storage
-----

The War plugin exclusively uses SQLite format 3 files to store data in the plugins/War folder.

War global settings, zone defaults, list of loaded warzones, and all other central data
is stored in the main database as war.sl3.

Each warzone is stored independently in a warzone file in the plugin folder. When a warzone is
deleted, a trash folder is created inside of the plugin folder and the file is moved to that
location. Administrators must regularly monitor their disk usage.

The bigger the warzone, the more space it will occupy on disk. Fortunately, War 2.0 uses
significantly less storage space than its predecessor. However, enormous zones will still
cause load on the server when blocks are saved and reset. The administrator may limit the
volume of all warzones with a configuration option.

**TAKE REGULAR BACKUPS OF YOUR DATA!** Especially during plugin updates.