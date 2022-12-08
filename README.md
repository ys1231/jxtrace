# jxtrace
1. 对不加壳的app java所有Api进行hook 获取参数返回值等.
2. 部分加壳app hook不到 可针对性修改.

# 使用方法 

1. `adb logcat | grep iyue_HookMain`
2. 可在搜索 `android.os.Debug`  过滤不需要的类名 或者 只过滤需要的类 方法等.
