# FtbCProtect
FTB区块加强保护
这是一个1.21.1Neoforge模组，用于给使用FTB区块模组的服务器提供更强的额外保护。
可以让服务器尽可能少禁用物品，更加开放。

它具有以下三个功能：
当玩家位于**没有权限的FTBC领地内**被禁止使用含有`#FtbCProtect:Prohibited_Item`标签的物品。

当玩家位于**没有权限的FTBC领地附近一定距离内（可配置）** 被禁止使用含有`#FtbCProtect:Prohibited_ItemInRegion`标签的物品。

当玩家位于**没有权限的FTBC领地附近一定距离内（可配置）** 被禁止放置含有`#FtbCProtect:Prohibited_BlockInRegion`标签的方块。

理论上他可以不需要kubejs做前置，但是如果不安装kubejs，你需要安装额外的模组或插件来给物品或方块添加Tag标签

Kubejs脚本示例:
```
// 禁止玩家在没有权限的FTB区块使用的物品
ServerEvents.tags('item', event => {
    event.add('FtbCProtect:Prohibited_Item', [
        'botania:managun', //魔力脉冲枪
        'botania:mana_bottle' //魔力瓶
    ])
}),
// 禁止玩家在没有权限的FTB区块附近使用的物品
ServerEvents.tags('item', event => {
    event.add('FtbCProtect:Prohibited_ItemInRegion', [
        'botania:managun', //魔力脉冲枪
        'botania:mana_bottle' //魔力瓶
    ])
}),
// 禁止玩家在没有权限的FTB区块附近放置的方块
ServerEvents.tags('block', event => {
    event.add('FtbCProtect:Prohibited_BlockInRegion', [
        'mekanism:digital_miner' //数字型采矿机
    ])
})
```
