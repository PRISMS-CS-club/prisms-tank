# Bot APIs

## Properties and Methods:

## visibleElements

- **Returns**: `Future<List<GameElement>>`
- **Serial Name**: `VisEle`

## visibleTanks

- **Returns**: `Future<List<Tank>>`
- **Serial Name**: `VisTks`

## visibleBullets

- **Returns**: `Future<List<Bullet>>`
- **Serial Name**: `VisBlts`

## visitedElements

- **Returns**: `Future<List<GameElement>>`t
- **Serial Name**: `VisEle`

## tankHp

- **Returns**: `Future<Int>`
- **Serial Name**: `TkHP`

## tankMaxHp

- **Returns**: `Future<Int>`
- **Serial Name**: `TkMxHP`

## tankLeftTrackSpeed

- **Returns**: `Future<Double>`
- **Serial Name**: `LTrkSpd`

## tankRightTrackSpeed

- **Returns**: `Future<Double>`
- **Serial Name**: `RTrkSpd`

## tankTrackMaxSpeed

- **Returns**: `Future<Double>`
- **Serial Name**: `TrkMxSpd`

## tankColBox

- **Returns**: `Future<ColRect>`
- **Serial Name**: `TkColBox`

## tankPos

- **Returns**: `Future<DPos2>`
- **Serial Name**: `TkPos`

## tankAng

- **Returns**: `Future<Double>`
- **Serial Name**: `TkAng`

## weaponRldTimePerSecond

- **Returns**: `Future<Double>`
- **Serial Name**: `WpnRld`

## weaponCurCapacity

- **Returns**: `Future<Int>`
- **Serial Name**: `WpnCurCap`

## weaponMaxCapacity

- **Returns**: `Future<Int>`
- **Serial Name**: `WpnMxCap`

## weaponColBox

- **Returns**: `Future<ColRect>`
- **Serial Name**: `WpnColBox`

## combinedColBox

- **Returns**: `Future<ColPoly>`
- **Serial Name**: `CombColBox`

## bulletColBox

- **Returns**: `Future<ColRect>`
- **Serial Name**: `BltColBox`

## bulletSpd

- **Returns**: `Future<Double>`
- **Serial Name**: `BltSpd`

## tankVisRg

- **Returns**: `Future<Double>`
- **Serial Name**: `TkVisRg`

## hpMoneyIncRate

- **Returns**: `Future<Pair<Double, Double>>`
- **Serial Name**: `HPMonIncRate`

## checkBlockAt

- **Parameters**: `pos: IVec2`
- **Returns**: `Future<Block>`
- **Serial Name**: `ChkBlkAt`

## checkCollidingGameEles

- **Returns**: `Future<ArrayList<GameElement>>`

- **Serial Name**: `ChkColEle`

## fire

- **Description**: Initiates a fire request.
- **Serial Name**: `Fire`

## getTankAndWeaponInfos

- **Parameters**: `type: TankWeaponInfo`
- **Returns**: `Future<ArrayList<*>>`
- **Serial Name**: `GetTkWpInfo`

## setDebugString

- **Parameters**: `str: String`
- **Description**: Sets a debug string.
- **Serial Name**: `SetDbgStr`

## setLeftTrackSpeed

- **Parameters**: `speed: Double`
- **Description**: Sets the left track speed.
- **Serial Name**: `SetLTrkSpd`

## setRightTrackSpeed

- **Parameters**: `speed: Double`
- **Description**: Sets the right track speed.
- **Serial Name**: `SetRTrkSpd`