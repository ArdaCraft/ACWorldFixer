|Options |Description|
|--|--|
|Level File|A level.dat file that stores the block registry that will be used in the final converted world|
|World Dir|The world save folder that will be converted|
|Output Dir|The directory where the converted world will be saved to|
|Load Config|Load block conversion rules from a previously saved configuration file|
|Edit Config|Open the configuration editor where you can add/remove/change block conversion rules|
|Save Config|Save your current set of block conversion rules to a file so that they can be re-used in the future|
|CPU Cores|Set the maximum number of threads the converter can use. The more cores allowed, the faster the overall conversion should be (may slow down computer performance for other programs whilst the conversion is underway)|
|Auto-Remap|Where blocks exist with one block id ('A') in the world save, and a different block id ('B') in the 'Level File', replace all instances of that block from ID 'A' to ID 'B'. Metadata values are not touched. User defined conversions take precedence over auto-remaps|
