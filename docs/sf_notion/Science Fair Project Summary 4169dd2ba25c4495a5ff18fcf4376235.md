# Science Fair: Project Summary

This document introduces detailed works that have been done and works planned to be done. The document is not an overall description of the idea, goal, background, and rationale of the project. It is more about the technical details that enabled us to reach our goal. It is supposed to be read after the project description and plans.

If you would like to know more detailed information about the project, you’re welcomed to check out our GitHub repository:

- Backend: https://github.com/prisms-cs-club/PRISMS-tank
- Frontend: https://github.com/prisms-cs-club/pristank-frontend

# Finished Works

## GUI (frontend)

We used react + typescript for displaying the game state on the screen, which allow us to easily integrate our game to websites. The GUI mainly constitutes of these parts:

1. Boot loader: load all relevant resources (images, properties, etc.) to the game
2. Game display: displays the current elements, blocks, tiles, etc. onto the screen using `pixi.js`.
3. Side panel: displays player information (hp, money, vision, etc.) and market progress (auction item, bid, etc.)
4. Event listener: listens and handles keyboard/gamepad events, then perform corresponding actions.
5. Communication: sends and receives packets through websocket with the game backend. It listens for and handles all the changes happening in the game, and sends the movements and other forms of requests to the game server.

![The game in booting phase](Untitled%204.png)

The game in booting phase

![The game in playing phase. The visible parts include game panel and side panels. The game panel is for displaying the current map of the game, while side panels for displaying informations of the game state.](Screenshot_2024-02-05_195743.png)

The game in playing phase. The visible parts include game panel and side panels. The game panel is for displaying the current map of the game, while side panels for displaying informations of the game state.

![Simplified structure of the GUI](Untitled_Diagram.drawio.svg)

Simplified structure of the GUI

Since the front end is completely open source, players have the freedom to design their own textures, key bindings, and maps for their customized game. We will post the detailed documentation on the website introduced later in this article.

Also, since the GUI connects to game core through the internet, the game can be easily played remotely with the GUI running on the client’s side and the backend running on the server’s side, making this game flexible and portable.

The game also incudes a separate map editor for users to create their own maps for testing or for fun.

![Untitled](Untitled%202.png)

In the map editor, you can select blocks or regions to place on the map. You can also use the ‘symmetry’ feature to quickly create a symmetric map.

## Game Core (backend)

The game core is the backend of the game system. All calculations and recordings are implemented there. The game core communicates with the GUI through WebSocket.

![The overall structure of Game Core ](sciencefair_proj_summary_gamecore_1.svg)

The overall structure of Game Core 

The above figure shows the overall structure of the game core.  The core is split into many different threads. Here, to show the main structure, some auxiliary threads like replay file recording and message dispatching are ignored. 

Unlike many other similar game-based coding competition platforms like Battlecode, our game is not round-based, meaning that the bot can make moves at arbitrary moments instead of waiting for a particular round. This is implemented by a multi-thread / process and request-response model. For each bot, either a system integrated like `HumanPlayerBot` or a user-written bot, there will be a `Controller`  variable passed into the `GameBot` object. Through the `Controller`, bot will be able to submit requests using a set of APIs to the server’s main thread, where each frame is calculated. The submitted request will be first store in a `BlockingQueue`, a thread-safe queue implementation.

The interaction between bot1 and the main thread in the figure will be first explained. Some functions in the main thread will be called once a time interval to maintain a fixed frame rate (the default is 60fps). In this interval, the processing function will check every request and process it. When the request is finished, the processing function will call the `complete` method of the value previously returned to the bot through `Controller` . All return values from `Controller` will be wrapped by `Future` , so that the API calls will not be blocking.

The game supports bot vs bot, human vs. human, and bot vs. human competitions by implementing different types of bots. The `HumanPlayerBot` , sharing the same interface as normal bots, listens to WebSocket ports and will immediately forward messages received as a request to the main thread. When receiving new events, the `HumanPlayerBot` will forward the serialized version of the event to the browser, where GUI si implemented. 

![Untitled](Untitled%205.png)

The above figure is the brief UML diagram of the game core. Since the time to write this document is limited a lot of other detailed implementation is covered. Such as a crude Game engine providing collision detection with various acceleration techniques like quadtrees and the implementation of the economy system. 

## Replay

The game can record a replay file by which the whole game can be reconstructed. The replay file is a large `JSON` array that contains all the events happened in the game, ordered by its time. For example, the following code shows a sample replay file:

```json
[
{"type":"Init","t":0,"plr":{"money":100,"mHP":150,"visRad":4.0,"tkArea":114514,"tkEdgeCnt":3,"tkSpd":3.0,"APItoken":114514,"w.capa":20,"w.dmg":10,"w.launchRt":10,"w.reload":3.0,"w.bltSpd":8.0,"w.bltWid":0.08},"pricingRule":"auction"},
{"type":"MapCrt","t":0,"x":15,"y":15,"initUid":0,"map":["SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","","","","BrkBlk","","","BrkBlk","","","BrkBlk","","","","SldBlk","SldBlk","","","","BrkBlk","","","BrkBlk","","","BrkBlk","","","","SldBlk","SldBlk","","","","BrkBlk","BrkBlk","","BrkBlk","BrkBlk","","","","","","SldBlk","SldBlk","BrkBlk","BrkBlk","","","BrkBlk","","","","","","","BrkBlk","BrkBlk","SldBlk","SldBlk","","","BrkBlk","","","SldBlk","","SldBlk","","","BrkBlk","BrkBlk","","SldBlk","SldBlk","","","BrkBlk","","SldBlk","","","","SldBlk","","","","","SldBlk","SldBlk","","BrkBlk","BrkBlk","","","","","","","","BrkBlk","BrkBlk","","SldBlk","SldBlk","","","","","SldBlk","","","","SldBlk","","BrkBlk","","","SldBlk","SldBlk","","BrkBlk","BrkBlk","","","SldBlk","","SldBlk","","","BrkBlk","","","SldBlk","SldBlk","BrkBlk","BrkBlk","","","","","","","BrkBlk","","","BrkBlk","BrkBlk","SldBlk","SldBlk","","","","","","BrkBlk","BrkBlk","","BrkBlk","BrkBlk","","","","SldBlk","SldBlk","","","","BrkBlk","","","BrkBlk","","","BrkBlk","","","","SldBlk","SldBlk","","","","BrkBlk","","","BrkBlk","","","BrkBlk","","","","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk","SldBlk"],"incMap":{"hp":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,10,10,0,0,0,0,0,0,0,0,0,0,0,0,10,20,10,0,0,0,0,0,0,0,0,0,0,0,0,10,10,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],"money":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,10,0,0,0,0,0,0,0,10,10,0,0,0,0,10,10,0,0,0,0,0,0,0,10,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,10,0,0,0,0,0,0,0,10,10,0,0,0,0,10,10,0,0,0,0,0,0,0,10,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]}},
{"type":"EleCrt","t":0,"uid":107,"name":"Tk","player":"RandomMovingBot","x":4.5,"y":5.5,"rad":0.0,"width":0.6,"height":0.6},
{"type":"EleCrt","t":0,"uid":108,"name":"Tk","player":"Tom","x":10.5,"y":9.5,"rad":0.0,"width":0.6,"height":0.6},
{"type":"EleCrt","t":0,"uid":106,"name":"Tk","player":"TankAimingBot","x":5.5,"y":5.5,"rad":0.0,"width":0.6,"height":0.6},
{"type":"EleCrt","t":0,"uid":109,"name":"Tk","player":"AuctTestBot","x":6.5,"y":11.5,"rad":0.0,"width":0.6,"height":0.6},
{"type":"MktUpd","t":4,"toSell":["mHP",true,13],"minBid":1,"endT":10003},
{"type":"EleUpd","t":113,"uid":107,"x":4.499,"y":5.524,"rad":0.121},
{"type":"EleUpd","t":121,"uid":107,"x":4.499,"y":5.524},
{"type":"EleUpd","t":125,"uid":107,"x":4.499,"y":5.524},
{"type":"MktUpd","t":133,"bidder":109,"price":1},
{"type":"EleUpd","t":152,"uid":107,"x":4.498,"y":5.529,"rad":0.146},
{"type":"EleUpd","t":158,"uid":107,"x":4.498,"y":5.529},
{"type":"EleUpd","t":181,"uid":107,"x":4.497,"y":5.533,"rad":0.164},
{"type":"MktUpd","t":191,"bidder":109,"price":2},
{"type":"EleUpd","t":195,"uid":107,"x":4.497,"y":5.535,"rad":0.174},
{"type":"EleUpd","t":203,"uid":107,"x":4.497,"y":5.535},
]
```

The replay file can be loaded into GUI to view the replay content. Also, since the replay file is in JSON format, it is easily readable for human and helpful for debugging when they design their scripts.

# Planned But Not Done

## Game and Programming Language Interface

There are several bugs to be fixed and features to be added to the game. Those issues/features are found to be relatively minor and do not affect the game-playing experience that much. The issues include:

1. The tank’s collision volume shape might distort in specific angles (but the appearance is unaffected since we implement these two parts separately).
2. Tanks are difficult to control in regions with narrow passings due to their sharp edges.

And features we planned but not implemented yet:

1. Record the statistics of each tank’s performance in the game (for example, total damage made by the tank).
2. Limit the number of API calls a tank can make during a limited amount of time. The API count is upgradable 
3. Make the tank’s size scalable.
With careful evaluation, we decided that this feature is extremely difficult to implement due to collision and overlapping with the surroundings. Therefore, we would like to delay the progress on this.
4. Make the game replay able to adjust speed, roll back, pause, and resume.
5. Implement the *victory score* system for our game to provide more variety in game strategies to win. We plan to implement it as follows: first, each player starts with a certain amount of victory scores, whose total amount is guaranteed by game rules to stay the same throughout the game. Whenever a player A is killed by another player B, all A’s victory score will transfer to B. Meanwhile, the game will also hold auctions to accelerate the transfer of victory scores: players can bid a certain price for a victory score, and whoever bids the highest will get 1 score from the current player with the lowest victory score. Whenever the victory score of a player drops to zero, the player automatically get killed.

## Website and Forum

We barely started working on the official website before the submission of our project. The website should be able to serve the following purposes:

1. The website will host online contests open to all registrations. Teams should be able to register their account, submit their coded strategy for bots, challenge other teams, review their matches with other teams, and receive a rating among all the other players with different strategies.
2. To make the game more interactive, there should also be options for users to play with  other bots, through which they can learn other teams’ strategies and to intuitively feel the power of programming.
3. We will design challenging problems for users to solve, such as asking users to implement path-finding algorithms and moving the tank to designated places. These challenges can help users familiarize themselves with our game and also serve as a programming practice for them.
4. The website will also include a forum for users to discuss programming techniques and solutions to problems and provide issues and feedback.

To be specific, our plan for the website includes these pages:

1. Team registration page
2. Team login page
3. Team information page
4. Online contest & code submission page
5. Contest replay page
6. Team rating & contest history page
7. Online human-vs-bot challenge page
8. Problem list
9. Problem detail and code submission page
10. Discussion and solutions page
11. Online game map editor

However, implementing all these parts is not easy work. There are still several technical difficulties we need to get through:

1. Human-vs-bot and human-vs-human competitions can be performed on the user’s side (the user downloads the code and runs it locally) or on the server’s side (the user opens a web page that communicates in real-time with the game’s host). Both method have their advantages and drawbacks:
    
    If these matches are performed on the server’s side, users will need fewer operations on their computers and can start the game instantly. The server can also ensure the security and fairness of the game, banning potentially hazardous operations such as shutting down the computer or creating memory leaks. The tradeoff to this convenience is that the game might have higher latency, less customization for the user, and more demand for computation power for the server.
    
    To balance the need, we plan to make both options available for the user. Users can choose to compete online or download the code and compete locally.
    
2. The design of the team rating needs careful tweaking. Each team’s strategy will be unique and has their own style (for example, some people may prefer directly attacking the enemy while others may prefer gathering more resources before action), while projecting all these strategies on a single axis will lose a major proportion of these details. It is against our ultimate goal to let every team only focus on the score while ignoring the innovations made in others’ strategies, but meanwhile, teams need a way to incentivize them to design better and more competent strategies.
We decide to rate each team on their performance with other teams. Every team is visible to others, and every team can choose to initiate a competition with others. If a team wins a competition, their score will increase while their opponents’ score will decrease. The amount of increase or decrease depend on their score difference before the match, so that the team originally having higher score will gain less if it wins a team with lower score. By this mechanism, we can ensure that all the teams will eventually converge to their designated places on the team rating.
3. The online forum needs to be regularized to prevent hazardous and unwanted information. We plan to incorporate AI bots to the forum to examine the content and filter out messages unrelated to the discussion topic.

Lastly, for investigative purposes, we will constantly send out surveys to different individuals to evaluate their programming performance and collect their feedback. Through their feedback, we can further analyze whether the game has a statistically significant improvement to users’ programming skills.

# Appendix

# Communication: Server to Client

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
- This event must have a timestamp of 0. Both replay and real-time games should begin with an `InitEvent`. Even if multiple events’ timestamps are 0, `InitEvent` should be the first in the list.

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

- `serialName = `dbg``
- `msg` : string containing debug information

# Communication: Client to Server

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

#