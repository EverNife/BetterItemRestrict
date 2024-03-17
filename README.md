## BetterItemRestrict
An Item Restrict plugin for Bukkit and Sponge. You can restrict players from owning or using a certain item. This plugin is made for modded servers (Cauldron-like, or SpongeForge).  
Item restrictions can be bypassed with the "betteritemrestrict.bypass" permission (for all items) and "betteritemrestrict.bypass.MATERIAL_NAME" for bypassing a specific item. On Sponge, replace any ":" on the item name with "-". If your permission plugin supports world permissions, you can allow items in certain worlds and restrict it in other worlds.   
Also the /banneditems command will show a list of banned items.

### Dependency
This plugin requires EverNifeCore

### Install
Download and copy the jar file in your plugins folder (under mods/plugins/ for Sponge servers).  
After loading the plugin for the first time, a default config will be generated.  
Edit your config and add the 

#### Permissions
- betteritemrestrict.list = for the "/banneditems" command
- betteritemrestrict.manage = for the "/bires" command
- betteritemrestrict.admin = for being notified in-game when a banned item is used

#### I will not be supporting on this plugin anymore

I have a paid version of this one called ItemExilum that does the same thing but with more features.