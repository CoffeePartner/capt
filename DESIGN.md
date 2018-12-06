# 写代码过程中的笔记（Release 时改为设计思想）

1. Proxy

1. 生成 Interceptor 类

2. 多个 Proxy 方法生成一个调用目标方法的 Interceptor 类，如果目标方法非public,生成一个 public synthetic 方法（虚拟方法）

3. 记录 synthetic 方法和 对应的目标方法，生成前检查父类有没有对应的方法(protected/public, method name, desc 一样)，如果有直接记录&调用


2. Insert

1. 对目标方法生成伴生(associated)方法(private)和虚拟方法，代码转移到伴生方法

2. 每个目标类生成一个内部类调用虚拟方法的 Interceptor 类

3. 不复用父类的，因为每个伴生方法都是private的


3. TryCatch

1. 生成 Interceptor 类

2. 不走全局的 GlobalInterceptor



4. exclude:

  1. 实现了 Interceptor 接口的类

  2. com.dieyidezui.lancet 包名下的类

  3. 有 Hook 方法的类

  4.



5. 记录类对应的文件 和 JarInput or DirectoryInput(name)


虚拟方法：synthetic public static 方法，用于跨权限





Graph



classes(basic , hook info, hooked info)

hook info => Hook Annotation -> Info


generated info


jarInputs : classes index



dirInputs : classes index





Info Processor => Process class bytes


class Proxy  -> remove add(定点)


class Insert  -> remove update add(定点)


class TryCatch  -> remove update add(定点)


Generated Interceptor

1. 1 Hook Method = 1 Interceptor(insert proxy try_catch)
2. 1 Insert matched method = 1 Interceptor(invoke synthetic)
3. 1 Proxy matched method = 1 Interceptor(invoke synthetic or invoke directly)


Proxy
直接要算出需要生成几个拦截器，在处理 Proxy 源方法时，
在调用处，直接指定调用目标拦截器，Hook 代码拦截器，看有没有匹配的

Insert 
生成的 伴生方法 权限 access 保持一致，这样就不一定需要虚拟方法了。



List<ClassInfo>

List<InputInfo>

List<HookClassInfo>

List<GeneratedInterceptorInfo>

List<GeneratedStaticEntranceInfo>


Remove @NameRegex

Add @Restrict, A annotation of annotation to describe the point cut hook if

@Restrict can easily create @NameRegex
