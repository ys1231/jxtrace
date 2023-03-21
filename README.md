# jxtrace
1. 对不加壳的app java所有Api进行hook 获取参数返回值等.
2. 部分加壳app hook不到 可针对性修改.

# 使用方法 

1. `adb logcat | grep iyue_HookMain`
2. 可在源码搜索 `android.os.Debug`  过滤不需要的类名  或执行下面的指令 添加只需要hook的类 比如com等前缀
3. `adb shell "echo classname > /data/local/tmp/hookPackage"`
# 预计新增
1. 重新实现查找类或增加反射调用加载的类方法.
