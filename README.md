# SuperDemo
Android设备获取root权限后通过app_process启动java程序设置默认launcher
1、使用前需要root
2、在sdk/build-tools目录下使用 dx --dex --output=superlibrary_dex.jar classes.jar 命令将jar包转换成可以被Dalvik加载的superlibrary_dex.jar
3、将superlibrary_dex.jar放在superdemo的assets目录下，编译生成apk安装运行即可
