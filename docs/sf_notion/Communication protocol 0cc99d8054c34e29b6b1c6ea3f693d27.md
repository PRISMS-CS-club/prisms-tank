# Communication protocol

If not specified, all the messages are in the UTF-8 charset.

# Server to GuiClient

Every game event is passed through the network using JSON format.  

In the Event base class, there are these properties:

- `t` (Long): the timestamp (in milliseconds) from the game starting to this event occurring.

These abstract methods:

- `serialize` (String): encodes the event into JSON format.
- `serialName` (String): The name of this event displayed on the JSON packet. Every event should have a unique serial name and should be as short as possible.

The server should ensure that all events are sent to the client and stored in the replay file by temporal order.

The JSON encoding of every event should include these two properties:

```json
{
  "type": "EleCrt",  // serial name of ElementCreateEvent
	"t": 0,            // `type` and `timestamp` are required for every event
}
```

These events extend from the event base class, and the following are events that the game core sends to the GUI:

## `InitEvent`

Initialize player and market information

- `serialName = "Init"`
- `pricingRule`: string. The pricing rule of the game. For more detail, check “Market and Upgrade”.
- `plr`: nested player update event, indicate the default properties of tanks in this game. This field is an object, whose format is the same as `PlayerUpdateEvent` except that it doesn’t have the timestamp and type field.
- This event must have a timestamp (`t`) of 0. Both replay and real-time games should begin with an `InitEvent`. Even if multiple events’ timestamps are 0, `InitEvent` should be the first in the list.

## `MapCreateEvent`

When creating a new map.

- `serialName = "MapCrt"`
- `x`: Width of the map. The maximum x coordinate of every element should be within [0, x).
- `y`: Height of the map. The maximum y coordinate of every element should be within [0, y).
- `initUid` (optional): the UID of the first non-empty block in the array. The UIDs of the following non-empty blocks increase in order. If this field is not set, let the initial UID be 0.
- `map`: A 1D array of strings (the serial name of the game element, which can be found later in this document) or nulls representing all the blocks in the map. The first element in the array is the top-left grid. If a grid does not have any block, put a `null` or an empty string in the corresponding array index.
- `incMap` : a JSON element of `string -> 1D array` mapping. Each string in the key is a property of the tank described in `UpgradeType`. The array after the string is the health increase speed in each tile, with the same order as the previous `map` array.
- For example:

```json
"incMap" : {
	"hp" : [1.114, 1.514, 
	        1.191, 1.981],
	"money" : [1.114, 1.514, 
	          1.191, 1.981]
}
```

## `ElemCreateEvent`

When creating an element in the game.

- `serialName = "EleCrt"`
- `uid`: The UID of the new game element.
- `name`: serial name of the element.
- `x`: X coordinate of the element
- `y`: Y coordinate of the element
- `rad`: Angle (in radiance) of the element. Angle 0 means pointing right (positive-x).
- `width` (optional): width of the element. If empty, use the default width in `element-data.json`.

- `height` (optional): height of the element. If empty, use the default height in `element-data.json`.
- If the event is creating a new tank, it should include this additional field:
    - `player`: The name of the player controlling this tank.
    - `plr`: An object nested in the `EleCrt` event. The format of this object is the same as `PlayerUpdateEvent`. For the field not included, the GUI will use the default value from the `Init` event.

## `ElemUpdateEvent`

When the position or hp of an element is changed, use this to update.

- `serialName = "EleUpd"`
- `uid`: The UID of the updated game element.
- `hp`: (optional): The new health point after the update
- `x` (optional): The new x coordinate of the game element. (in double)
- `y` (optional): The new y coordinate of the game element. (in double)
- `rad` (optional): The new angle in the radiance of the game element. (in double)

## `ElemRemoveEvent`

To remove some game elements from the GUI screen

- `serialName = "EleRmv"`
- `uid`: The UID of the element to remove

## `PlayerUpdateEvent`

 update various special properties of players (tanks), such as visible range in the map. Note that the change of any upgradable elements is informed to the GUI through this event. Each of the following updatable items has a `serialName` and will be used when contained as fields in this event. Besides `uid`, all fields for this event are optional. 

- `serialName = PlrUpd`
- `uid`: UID of the player to update.
- `money`: the remaining amount of money for a player.
- `visRad`: vision radius of a tank.
- (for future) might need to add more for upgrades of tanks
- `mHP`: maximum HP of the player after this update.
- weapons: see the following text (all updates relating to weapons contains a prefix of `w.`)
- `tkArea`: size (area) of tank body
- `tkEdgeCnt`:  the shape of the tank body (users could change the number of edges of polygons, notice that the area of the tank body will not be changing)
- `tkSpd`:  moving speed of tanks.
- `APItoken`:  API call token per second
- `dbgStr`: Debug string. This string will be displayed on the screen only for debugging purposes.

specifically for weapons, these features are upgradable:

- `w.capa`: maximum bullet capacity
- `w.launchRt`: launching rate
- `w.reload`: refill rate (time taken to fill up available bullets to their maximum capacity)
- `w.numBarrel`: number of barrels
- `w.dmg`:  damage per bullet
- `w.bltSpd`: flying speed of bullets
- `w.bltWid`: width of the bullet

## `MarketUpdateEvent`

When one player buys something in the market

- `serialName = "MktUpd"`
- The detail of this event depends on the game’s rule (could be referred to in the “Market” page of the documentation)

## `GameEndEvent`

When the game ends.

- `serialName = "End"`
- `uids`: array. All player’s UIDs in the order of their ranking in the game, from best to worst.
- `rank`: array, optional. `rank[i]` means the ranking of `uids[i]`, starting from 1, since there might be multiple players with the same ranking. The game should make sure that `rank` has the same length as `uids`. If `rank` is not provided, then no two players in the `uids` array have the same rank, or: the `i`th player’s rank is exactly `i`.

## `DebugEvent`

This event does not have any actual uses, it is purely for debugging purposes. It is printed in the replay file so that debug information could be recorded with time.

- `serialName = `Dbg``
- `msg` : string containing debug information

# GuiClient to Server

## Initializing

Immediately after the socket connection is established, the client should send a packet containing one single string.

- If the string is empty, the client will be in observation mode. The game would not respond to any of its actions.
- If the string is not empty, the client will be in playing mode. The game will treat the client as a player.

## Requests

GUI should send the user’s event through the web socket in the following format:

- Each packet is a separate request.
- A request consists of the request’s time stamp, name, and parameters.
- One request name uniquely corresponds to one function in the `Controller` interface.

For example, The following text is a series of requests:

```
1000 lTrack 1.0
1000 rTrack 0.8
1124 fire
1250 lTrack 0.7
1250 rTrack 0.7
2000 fire
```

All requests are listed here:

- `lTrack <speed:double>`
    - Set the speed of the left track to `speed`
    - `setLeftTrackSpeed` in `Controller`
- `rTrack <speed:double>`
    - Set the speed of the right track `speed`
    - `setRightTrackSpeed` in `Controller`
- `fire`
    - `fire` in `Controller`
- [`marke](http://market.bid)t.<>` market actions. The format depends on the specific rule currently used. See the “market” section for more details.

# BotController to Server

In the new version of the backend, bots are running in different processes, so the communication is through TCP. 

Since TCP is a stream-based protocol, we need a method to play the role of delimiter. So that before anything is sent there is a header in the following structure

```python
struct{
	int32 body_size;
	int64 cmd;
};
```

The `cmd` part represent some additional functionalities, it is currently a reserved field. 

like the previous events, there is always a field indicating time

```json
{
name : "init", 
teamID : 114514,
t: 1919810
}
```

Notice that the `t` here is the game time. 

## `BotInitEvent`

- `serialName = bInit` : string
- `name`: string, the name of the team

## `BotRequestEvent`

- `serialName = bReq`
- `reqType` : string. This is the serial name for the API
- `params`  : string. Binary generated by serializers like Kyro converted to strings in base64 encoding. So, it is fine to accept more complicated data types as parameters.
- `rid`: long. A unique ID. It is not ensured that the requests returned by the server follow a chronological sequence. Although the `t` field already specified the time in milliseconds, it might be possible that there are multiple requests within one second.

# Server to BotController

## `ServerSyncronizeEvent`

- `serialName = sSync`
- `initT`  : long. Starting time of the game.

## `ServerResponseEvent`

- `serialName = sRes`
- `retVal` : string. Binary generated by serializers like Kyro converted to strings in base64 encoding.
- `rid`  : Long, same as the rid in `BotRequestEvent`
- `sentT` : Long. time when the request is sent