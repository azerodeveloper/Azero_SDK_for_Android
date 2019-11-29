# 快速试用

Azero for Android 可支持在绝大多数Android设备直接安装试用，可体验Azero 默认的通用技能，覆盖了主流语音交互的大部分能力，可帮助开发者快速体验Azero的主要功能、初步了解语音交互的基本流程。

## 目录

* [前提条件](#prerequisites)
* [直接安装](#install)
* [注册设备](#account)
* [工具准备](#tools)
* [使用步骤](#step)
* [快速Azero](#test)
* [更多技能](#more_skill)
* [进阶集成和调优](#advanced)
* [已知问题](#known_issue)
* [下载](#download)

## 试用前提<a id="prerequisites"> </a>

- Android 4.4 及其以上平台
- 支持标准的Audio Record API
- 支持麦克风


## 直接安装<a id="install"> </a>

  你可选择可直接下载 **Azero_Sample_for_Android.apk** ,安装到手机上进行试用。

  **注：当前sample app未进行竖屏适配，建议横屏使用*

##  注册设备<a id="account"> </a>

请遵循如下步骤去Azero开发平台完成设备的注册。
1. 登录到[Azero开放平台](https://azero.soundai.com)注册账号，账号需进行实名认证。

2. 接下来创建设备，请参考[设备接入介绍](https://azero.soundai.com/docs/document)进行设备注册。

   ![dev_reg](/docs/dev_reg.png)

3. 创建完毕后，在**设备中心 -> 已创建设备**页面可以看到创建的设备，点击对应设备的“查看”进入设备信息页面，页面中的“产品ID”项对应的值即为productId；"Client ID"项对应的值即为clientId。 



##  工具准备<a id="tools"> </a>

Android Studio 3.2及其以上版本

##  试用步骤<a id="step"> </a>

1）下载SDK以及示例程序包

2）如步骤<a id="prerequisites">注册设备</a> 所示，申请productID、clientId

3）将sample工程导入到Android Studio中

4）将申请的client id 、product id 填入MyApplication，并传入设备唯一标识，可使用提供的Utils.getimei方法获取设备imei ,如该方法无法获取，则需要自行传入设备唯一标识，其他默认即可。示例如下所示：

```java
Config config = new Config(
                    "",                 //示例，productID 网站申请
                    "",                 //示例，ClientID  网站申请
                    Utils.getimei(this),//DeviceSN 传入Mac地址或IMEI号，必须保证设备唯一
                    Config.SERVER.PRO,//Server 选择使用的服务器  FAT 测试环境 PRO 正式环境
                    Setting.enableLocalVAD//localVAD  是否使用本地VAD
            );
```

5）安装app到设备端

6）赋予app录音、读取设备权限

完成上述步骤，即可正常体验Azero 默认功能。

## 体验Azero<a id="test"> </a> 

经过上述步骤后，可以立刻体验Azero , 主要功能包括：

1. 唤醒。您可以对设备说"小易小易"唤醒设备，因为当前使用的单路mic数据，因此您需要距离设备稍近些进行唤醒。
2. 听歌。唤醒“小易”后，您可以对他说“我想听歌”等命令词。
3. 聊天。唤醒“小易”后，您可以问他一些问题，“小易”将与您进行聊天。
4. 如2,3 ,Azero默认支持绝大多数语音交互场景，你可以尝试与他进行更多的交流。

**注：部分设备因为系统权限问题，可能需要赋予弹窗（浮窗）权限，否则将无法显示唤醒框；*
       *另外，因为部分设备自身的回声消除效果欠佳，可能存在自体播放声音的情况下无法唤醒的现象，解决此问题，可参看进阶文档，提供回采信号进行回声消除，可以体验到更加效果*

##  更多技能<a id="more_skill"> </a> 
如需体验更丰富的技能和个性化定制体验，可去 **Azero开放平台 -> 技能商店** 为设备配置更多的官方或者第三方技能，或者按照 [技能接入介绍](https://azero.soundai.com/docs/document) 创建自有技能，实现定制化需求。

## 进阶集成和调优<a id="advanced"> </a> 

完成上述过程，您已经对Azero的语音基本交互过程有了初步体验，如需将Azero SDK集成到您的工程，并针对您的自有设备进行唤醒、识别等语音交互效果的调优请参照进阶文档。

- [声智Azero系统说明文档](./docs/声智Azero系统说明文档 - TurnkeySample.md)
- [Azero SDK API](./docs/javadoc/index.html)

##  已知问题<a id="known_issue"> </a>

- Version 1.0.1及其之前版本中Launcher background图片可能会失效，届时您可选择自行替换其背景图片，也可选择试用[补丁](./patch/launcher_backgroud.patch)更新之前图片的新地址。
- Azero 体验包中视频默认配置了蜜蜂视频App，如需体验视频交互，可以下载[公版蜜蜂视频](http://www.beevideo.tv/apk/bjPQbheOhd8%3D.html)体验；如需产品化，请联系我们获取支持。
- Version 1.0.2及其之前版本初次安装时，可能无法录音。遇到此问题，给予录音权限之后，需重启下app即可。

## 下载<a id="download"> </a>

如github的下载有问题的，可使用百度网盘下载压缩包：[Azero_SDK](https://pan.baidu.com/s/1LYSt4TcIxxhH-E0xfWKF8w)