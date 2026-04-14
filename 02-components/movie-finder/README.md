# MovieFinder — Spring IoC/DI 与软件构件

对应 slides 中 Martin Fowler 的 MovieFinder 案例，用纯 Spring（非 Boot）演示构件化开发的核心思想。

## 从构件定义看本项目

Szyperski 对软件构件的定义：

> A software component is **a unit of composition** with **contractually specified interfaces** and **explicit context dependencies** only. A software component can be **deployed independently** and is subject to **composition by third parties**.

本项目中的每个概念都能对应到这个定义：

| 构件特征 | 本项目中的体现 |
|---------|-------------|
| **A unit of composition** | `MovieLister` 和 `MovieFinder` 是独立的构件，组合后构成完整的电影查询功能 |
| **Contractually specified interfaces** | `MovieFinder` 接口就是契约——`MovieLister` 只依赖接口，不知道具体实现 |
| **Explicit context dependencies** | `MovieLister` 声明自己需要一个 `MovieFinder`（通过构造器参数或 setter），依赖关系显式可见 |
| **Deployed independently** | `ColonMovieFinder`（读文件）和 `InMemoryMovieFinder`（内存数据）可以独立开发、独立替换 |
| **Composition by third parties** | Spring 容器（Container）就是"第三方"——它负责把构件组装在一起，`MovieLister` 自己不 `new` 依赖 |

## 关键对比：Bad Design vs. Good Design

```java
// Bad: MovieLister 自己创建依赖 → 紧耦合，无法替换
private MovieFinder finder = new ColonMovieFinder("movies.txt");

// Good: 依赖由外部注入 → 松耦合，可灵活组装
@Autowired
public MovieLister(MovieFinder finder) {  // 接口，不是具体类
    this.finder = finder;
}
```

构件**绝不应该自己创建（实例化）它的依赖**。这就是 IoC（控制反转）的核心：创建和装配的"控制权"从构件自身反转到了容器。

## 项目结构

```
src/main/java/moviefinder/
├── Movie.java               # 领域对象
├── MovieFinder.java          # 接口（构件契约）
├── ColonMovieFinder.java     # 实现①：从文本文件读取
├── InMemoryMovieFinder.java  # 实现②：内存数据
├── MovieLister.java          # Constructor Injection（推荐）
└── MovieListerSetter.java    # Setter Injection

src/main/resources/
├── spring.xml                # XML 配置：装配 ColonMovieFinder
├── spring-inmemory.xml       # XML 配置：装配 InMemoryMovieFinder
└── movies.txt                # 电影数据（title:director）
```

## 运行测试

```bash
mvn test
```

## 5 个测试覆盖的知识点

| 测试方法 | DI 方式 | 配置方式 | MovieFinder 实现 |
|---------|--------|---------|----------------|
| `testConstructorInjection` | 构造器注入 | `@Component` + `@Autowired` | ColonMovieFinder |
| `testSetterInjection` | Setter 注入 | `@Component` + `@Autowired` | ColonMovieFinder |
| `testXmlConfiguration` | 构造器注入 | `spring.xml` | ColonMovieFinder |
| `testSwapImplementation_JavaConfig` | 构造器注入 | `@Bean` Java Config | **InMemoryMovieFinder** |
| `testSwapImplementation_Xml` | 构造器注入 | `spring-inmemory.xml` | **InMemoryMovieFinder** |

## 灵活组装的演示

最后两个测试是本项目的重点——**同一个 `MovieLister`，零修改，搭配不同的 `MovieFinder` 实现**：

- `ColonMovieFinder`：从 `movies.txt` 文件读取，查 Sergio Leone 的电影得到 2 部
- `InMemoryMovieFinder`：内存中的 Nolan 电影列表，查 Christopher Nolan 得到 3 部

这直接体现了松耦合带来的架构质量属性：

| 质量属性 | 体现 |
|---------|-----|
| **Modifiability** | 替换数据来源不改 `MovieLister` 一行代码 |
| **Testability** | 测试时注入 `InMemoryMovieFinder`，无需依赖真实文件 |
| **Reusability** | `MovieLister` 可在任何项目中复用，搭配任意 `MovieFinder` 实现 |
