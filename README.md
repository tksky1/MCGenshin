# MCGenshin 

MC原神插件-模仿原神元素反应系统机制的spigot插件

A spigot plugin that simulate the Element Reaction function in Genshin Impact

## 前言

MCGenshin是我突然脑洞的结果，用于给Mcyouyou公益群组服作为流动分服玩法。

本人初学java、git和bukkit插件，代码水平极其菜鸡，见谅~

## 介绍

插件引入了原神的元素和元素反应系统，并引入“神之眼” 系统，可以附魔到武器上，使得玩家可以操纵和使用元素辅助战斗，扩展游戏的可玩性。

### 一、元素

元素附着和元素之间的反应是本插件的核心机制。

#### 1. 元素附着

本插件为游戏增加共5种元素，分别是火、水、风、雷、冰。

元素可以通过多种形式附着在实体上。玩家可以通过使用神之眼对实体造成元素附着；怪物攻击玩家时会传递自身附着的元素给玩家；下雨、着火、踏水、踏雪等环境因素也可以给实体施加元素附着；部分生物，如末影人、末影螨、烈焰人等生物自然带有且维持一定数量的元素。

在玩家受到元素附着时，会在物品栏上方看到附着元素的提示；在其他实体受到元素附着时，可以在其名字栏看到其附着的元素。

元素附着的数量是定量计算的。各元素在实体上的附着相互独立。附着在实体上的元素会随时间自然减少，直至不再附着为止。

元素附着量的最小值为0，最大值为100；多次附着的元素量可以累加。

如果两种或多种元素同时附着在同一实体上，则会发生元素反应。

> 地脉是连接世界上一切事物的概念网络，不同的元素在其中奔流。

#### 2. 元素反应

插件共为游戏提供7种元素反应，分别是超载、超导、扩散、蒸发、融化、感电、冻结。

7种反应分为聚变反应和非聚变反应两种。非聚变反应包括蒸发与融化两种，其余反应均为聚变反应。

下面是各反应的详细介绍。

- 超载反应
  雷元素与火元素共存会发生超载反应。对雷、火两种元素进行等量最大化消耗，同时在发生反应的实体产生一次不破坏方块的小范围爆炸。

- 超导反应
  雷元素与冰元素共存会发生超导反应。对雷、冰两种元素进行等量最大化消耗，同时对发生反应的实体施加虚弱III效果5秒，并造成权值常数为2的元素伤害。

- 扩散反应
  风元素施加到实体时，立即清除全部风元素；若该实体还附着有其他元素，则立即发生扩散反应，根据风元素量是否<50，分为强扩散和弱扩散两种。不论强弱，均按元素顺序选定一个其带有的元素为目标扩散元素。强扩散时，清除反应对象的全部目标元素，并给近3格的实体（玩家除外）附着目标元素，数量为50.弱扩散时，如目标元素数量>50，则使其数量减少50，否则清除全部目标元素。弱扩散完成后给近3格的实体（玩家除外）附着目标元素，数量为20.

- 蒸发反应
  水元素和火元素会发生蒸发反应。若触发元素为火元素，则使触发反应的伤害获得基于其伤害量50%的元素伤害；若触发元素为水元素，则使触发反应的伤害获得基于其伤害量100%的元素伤害。反应后两元素等量最大化消耗。

- 融化反应
  冰元素和火元素会发生融化反应。若触发元素为冰元素，则使触发反应的伤害获得基于其伤害量50%的元素伤害；若触发元素为火元素，则使触发反应服伤害获得基于其伤害量100%的元素伤害。反应后两元素等量最大化消耗。

- 感电反应
  水元素和雷元素共存时，不会立即发生反应。但在元素自然消耗时，发生感电反应，使得两元素消耗速度提高100%，同时对发生反应的实体造成1权重的元素伤害，直至两者不再共存。

- 冻结反应
  水元素和冰元素共存时，不会立即发生反应。但在元素自然消耗时，发生冻结反应，使得两元素消耗速度提高100%，同时对发生反应的实体施加缓慢V效果，直至两者不再共存。

其中，涉及元素伤害的，遵循下列计算公式：

- 非聚变反应
  伤害 = 原有伤害+元素伤害；
  元素伤害 = 原有伤害 * 增伤比例（不克制50%或克制100%）* （消耗的元素附着量/50）*（元素精通/50）

- 聚变反应
  超载：爆炸伤害；
  感电：反应存在时持续给予固定1点伤害；
  其他反应伤害 =（ 反应消耗的元素量/50 ）* 常数（超导：2） * （元素精通/50）

元素反应遵循下列优先级：
超载>超导>扩散>蒸发>融化>感电>冻结

### 3. 元素爆发

根据神之眼的属性，使用神之眼对实体进行元素附着时，有一定概率产生元素爆发。元素爆发时，对该实体周围两格内的全部实体施加和神之眼属性同等的元素附着，但不产生元素伤害。



### 二、神之眼与元素精通

> 面对无法掌控的境遇时，人们总是喟叹自身的无力。但在人生最陡峭的转折处，若有凡人的「渴望」达到极致，神明的视线就将投射而下。这就是「神之眼」，受神认可者所获的外置魔力器官，用以引导元素之力。
>
> 每一位「神之眼」的拥有者，都是有资格成神的人，因此被称作「原神」，拥有登上天空岛的资格。

神之眼是玩家给实体施加元素附着的工具。神之眼获得后会自动放入背包或放在脚下（包满时），玩家会获得提示。

玩家可以直接在主手用神之眼攻击造成元素附着，也可以将神之眼通过`/useVision`命令将神之眼附魔到武器（或其他物品）上，使得用该武器（近战或远程）进行攻击时也能造成元素附着。

在剑的横扫或弩的多重射击时可能会命中多个目标，元素附着只会对最先命中的目标生效。

神之眼具有以下基本属性：

- 元素属性：为5种元素中的一种，决定了其能给实体附着的元素种类；
- 附着数量：单次触发元素附着给目标实体造成元素附着的数量；
- 附着冷却时间：触发元素附着后人物再次触发元素附着的冷却时间。该冷却时间以玩家为计算对象，在该神之眼决定的冷却时间内不可再使用任何神之眼造成元素附着，直到冷却时间结束。
- 元素精通：造成元素反应伤害的能力，完全由神之眼生成时获得的数值决定且不可改变。
- 元素爆发概率：触发元素爆发的概率，与元素精通成反比。

神之眼单次元素附着数量最小值为25，最大值为75；元素精通最小值为1，最大值为100.

附着数量和附着冷却时间大致成正比，随机生成神之眼时对这些数据会有少量随机偏量。

每个神之眼的各项属性在生成时随机决定，且不可更改。

> 正是因为无法更改，无可违逆只能接受，命运才会被称之为命运。



## 命令

- `/useVision`

所有人都有权限使用。该命令用于将已有的神之眼绑定（附魔）到未附魔过神之眼的武器上，使得该武器拥有施加元素附着的权能。

使用命令时，必须将要附魔的武器放在主手，将神之眼放在副手。附魔成功后，副手的神之眼将生效消失，同时主手的武器获得该神之眼的各项属性和权能。

神之眼附魔与武器原有的附魔等信息不冲突，一把武器最多被附魔一个神之眼。

- `/giveVision [player]`

需要`mcgenshin.givevision`权限。用于给某在线玩家发放一个属性随机的神之眼。

- `/mcgenshin`

展示插件信息。



