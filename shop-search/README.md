# 搜索服务



# 1.es基础

## 1.1.倒排索引

搜索引擎目前主流的实现原理：倒排索引技术

**倒排索引又叫反向索引**（如下图）以字或词为关键字进行索引，表中关键字所对应的记录表项，记录了出现这个字或词的所有文档，每一个表项记录该文档的编号和关键字在该文档中出现的位置情况。

![1563168654154](assets/1563168654154-1585445655013.png)

在实际的运用中，我们可以对数据库中原始的数据结构（如：商品表），在业务空闲时，事先生成**文档列表（左图）及倒排索引区域（右图）**。

用户有查询需求时，**先访问倒排索引数据区域**（右图），得出文档编号后，通过文档编号即可快速，准确的**通过左图找到具体的文档内容**。

 例如：用户输入“跳槽”关键字，先到右图的索引区查询，找到1,4；再根据id=1和id=4到左图找到两条记录。整个过程走的都是索引，比传统的链式匹配更加快速。

## 1.2.全文检索

![1563169765236](assets/1563169765236.png)

倒排索引是全文检索技术的一种实现方式。

## 1.3.docker安装es

ik分词器提供了重要的扩展能力：

**扩展词典**（新创建词功能）： 有些词IK分词器不识别 例如：“尚硅谷”，“蓝瘦香菇”，“碉堡了”

**停用词典**（停用某些词功能）： 有些词不需要建立索引  例如：“哦”，“啊”，“的”

elasticsearch加载ik分词器插件时，ik会读取一个配置文件，这个配置文件在ik分词器根目录的config目录下：

![1563235025521](assets/1563235025521-1585445795501.png)

打开该配置文件：

![1563235084668](assets/1563235084668-1585445795501.png)

这里有两种方式配置扩展词典和停用词典：

1. 本地方式：ext_dict配置扩展词典，ext_stopwords配置停用词典。
2. 远程方式：tomcat或者nginx

第一种方式每次修改词典都要重启搜索服务，不推荐。推荐使用nginx的方式，并发量大，修改内容不需要重启。

**第一步：利用nginx搭建远程词库。**

![1563236204059](assets/1563236204059-1585445819772.png)

需要在nginx根目录下创建对应的elasticsearch目录

![1563241622694](assets/1563241622694-1585445819772.png)

添加扩展词典，**每行一个关键词**：

![1563241667461](assets/1563241667461-1585445819772.png)

测试：

![1563237774843](assets/1563237774843-1585445819772.png)

**第二步：在ik分词器中引用远程词库**

进入ik分词器的conf目录：cd /usr/share/elasticsearch/plugins/ik-analyzer/config/

![1563242627129](assets/1563242627129-1585445819772.png)

**重启elasticsearch服务**，再次测试：

![1563242487412](assets/1563242487412-1585445819772.png)

添加新词条后，es只会对新增的数据用新词分词。历史数据是不会重新分词的。如果想要历史数据重新分词。需要执行：`POST {index}/_update_by_query?conflicts=proceed`



# 2.es操作

## 2.1.基本概念

|   名词   |                             说明                             |
| :------: | :----------------------------------------------------------: |
| cluster  | 整个elasticsearch 默认就是集群状态，整个集群是一份完整、互备的数据。 |
|   node   |         集群中的一个节点，一般只一个进程就是一个node         |
|  shard   | 分片，即使是一个节点中的数据也会通过hash算法，分成多个片存放，默认是5片。 |
|  index   | 索引。相当于rdbms的database, 对于用户来说是一个逻辑数据库，虽然物理上会被分多个shard存放，也可能存放在多个node中。 |
|   type   | 类似于rdbms的table，但是与其说像table，其实更像面向对象中的class , 同一Json的格式的数据集合。 |
| document |         文档。类似于rdbms的 row、面向对象里的object          |
|  field   |                    字段。相当于字段、属性                    |
| mappings |     映射。字段的数据类型、属性、是否索引、是否存储等特性     |

与关系型数据库对比：

* 索引（indices）---------------------- Databases 数据库
* 类型（type）-------------------------- Table 数据表
* 文档（Document）---------------------- Row 行
* 字段（Field）------------------------- Columns 列   

数据结构对比：

* 这两个对象如果放在关系型数据库保存，会被拆成2张表

```java
public class  Movie {
    String id;
    String name;
    Double doubanScore;
    List<Actor> actorList;
}

public class Actor{
    String id;
    String name;
}
```

* 但是elasticsearch是用一个json来表示一个document，所以他保存到es

```json
{
    “id”:”1”,
    “name”:”operation red sea”,
    “doubanScore”:”8.5”,
    “actorList”:[  
        {“id”:”1”,”name”:”zhangyi”},
        {“id”:”2”,”name”:”haiqing”},
        {“id”:”3”,”name”:”zhanghanyu”}
    ]
}
```

要注意的是：Elasticsearch本身就是分布式的，因此即便你只有一个节点，Elasticsearch默认也会对你的数据进行分片和副本操作，当你向集群添加新数据时，数据也会在新加入的节点中进行平衡。



## 2.2.索引（indeces）

### 2.2.1.查询

`GET /_cat/indices?v`

![1563199326580](assets/1563199326580-1585446542869.png)

es 中会默认存在一个名为.kibana和.kibana_task_manager的索引

表头的含义：

|     字段名     |                           含义说明                           |
| :------------: | :----------------------------------------------------------: |
|     health     | green(集群完整) yellow(单点正常、集群不完整) red(单点不正常) |
|     status     |                          是否能使用                          |
|     index      |                            索引名                            |
|      uuid      |                         索引统一编号                         |
|      pri       |                          主节点几个                          |
|      rep       |                          从节点几个                          |
|   docs.count   |                            文档数                            |
|  docs.deleted  |                        文档被删了多少                        |
|   store.size   |                        整体占空间大小                        |
| pri.store.size |                           主节点占                           |

### 2.2.2.创建

`PUT /索引名`

参数可选：指定分片及副本，默认分片为5，副本为2。

```json
{
    "settings": {
        "number_of_shards": 3,
        "number_of_replicas": 2
    }
}
```

演示：说明索引创建成功

![1563200563246](assets/1563200563246-1585446619748.png)

再次查询，可以看到刚刚创建的索引：

![1563200665166](assets/1563200665166-1585446619748.png)

### 2.2.3.信息

`GET /索引名`

![1563200912527](assets/1563200912527.png)

### 2.2.4.删除

`DELETE /索引库名`

演示：

![1563201353271](assets/1563201353271-1585446708541.png)

查看atguigu:

![1563201443616](assets/1563201443616-1585446708541.png)



## 2.3.映射（_mapping）

映射是定义文档的过程，文档包含哪些字段，这些字段是否保存，是否索引，是否分词等

es中的所有字段都会保存到_source字段中

是否保存：取决于是否在查询结果集中展示该字段

是否索引：取决于是否以该字段搜索

是否分词：前提是字符串数据，必须以该字段进行检索。就应该使用分词（text），指定分词器（ik_max_word）

### 2.3.1.创建

```json
PUT /索引库名/_mapping/类型名称
{
    "properties": {
        "字段名": {
            "type": "类型",
            "index": true，
            "store": true，
            "analyzer": "分词器"
        }
    }
}
```

类型名称：就是前面将的type的概念，类似于数据库中的不同表

字段名：类似于列名，properties下可以指定许多字段。

每个字段可以有很多属性。例如：

- type：类型，可以是text、long、short、date、integer、object等
- index：是否索引，默认为true
- store：是否存储，默认为false
- analyzer：分词器，这里使用ik分词器：`ik_max_word`或者`ik_smart`

> 示例

发起请求：

```json
PUT atguigu/_mapping/goods
{
    "properties": {
        "title": {
            "type": "text",
            "analyzer": "ik_max_word"
        },
        "images": {
            "type": "keyword",
            "index": "false"
        },
        "price": {
            "type": "long"
        }
    }
}
```

响应结果：

```json
{
    "acknowledged": true
}
```

### 2.3.2.查看

> 语法：

```
GET /索引库名/_mapping
```

> 示例：

```
GET /atguigu/_mapping
```

> 响应：

```json
{
    "atguigu" : {
        "mappings" : {
            "goods" : {
                "properties" : {
                    "images" : {
                        "type" : "keyword",
                        "index" : false
                    },
                    "price" : {
                        "type" : "long"
                    },
                    "title" : {
                        "type" : "text",
                        "analyzer" : "ik_max_word"
                    }
                }
            }
        }
    }
}
```

### 2.3.3.属性

![1531712631982](assets/1531712631982.png)

* type：

  * String类型，又分两种：

    - text：可分词，不可参与聚合
    - keyword：不可分词，数据会作为完整字段进行匹配，可以参与聚合

  * Numerical：数值类型，分两类

    - 基本数据类型：long、interger、short、byte、double、float、half_float
    - 浮点数的高精度类型：scaled_float
      - 需要指定一个精度因子，比如10或100。elasticsearch会把真实值乘以这个因子后存储，取出时再还原。
  * Date：日期类型
    * elasticsearch可以对日期格式化为字符串存储，但是建议我们存储为毫秒值，存储为long，节省空间。

* index：

  * index影响字段的索引情况。

    - true：字段会被索引，则可以用来进行搜索。默认值就是true
    - false：字段不会被索引，不能用来搜索
  * index的默认值就是true，也就是说你不进行任何配置，所有字段都会被索引。
  * 但是有些字段是我们不希望被索引的，比如商品的图片信息，就需要手动设置index为false。

* store：

  * 是否将数据进行额外存储。
  * Elasticsearch在创建文档索引时，会将文档中的原始数据备份，保存到一个叫做`_source`的属性中。而且我们可以通过过滤`_source`来选择哪些要显示，哪些不显示。
  * 而如果设置store为true，就会在`_source`以外额外存储一份数据，多余，因此一般我们都会将store设置为false，事实上，**store的默认值就是false。**



## 2.4.文档（document）

### 2.4.1.自动生成id

语法：

```
POST /索引库名/类型名
{
    "key":"value"
}
```

示例：

```
POST /atguigu/goods
{
  "title": "华为手机",
  "images": "http://image.jd.com/12479122.jpg",
  "price": 4288
}
```

测试：

![1563203068066](assets/1563203068066-1585447750459.png)

查询看看结果：

![1563203218864](assets/1563203218864-1585447750460.png)

- `_source`：源文档信息，所有的数据都在里面。
- `_id`：这条文档的唯一标示，与文档自己的id字段没有关联

### 2.4.2.自定义id

如果我们想要自己新增的时候指定id，可以这么做：

```
POST /索引库名/类型/id值
{
    ...
}
```

演示：

![1563203408792](assets/1563203408792-1585447838841.png)

查询得到两条数据：小米手机的id是我们指定的id

![1563203504839](assets/1563203504839-1585447838841.png)

### 2.4.3.智能判断

事实上Elasticsearch非常智能，你不需要给索引库设置任何mapping映射，它也可以根据你输入的数据来判断类型，动态添加数据映射。

测试一下：

```json
POST /atguigu/goods/2
{
    "title":"小米手机",
    "images":"http://image.jd.com/12479122.jpg",
    "price":2899,
    "stock": 200,
    "saleable":true,
    "attr": {
        "category": "手机",
        "brand": "小米"
    }
}
```

我们额外添加了stock库存，saleable是否上架，attr其他属性几个字段。

来看结果：`GET /atguigu/_search`

```json
{
  "took" : 7,
  "timed_out" : false,
  "_shards" : {
    "total" : 2,
    "successful" : 2,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 2,
    "max_score" : 1.0,
    "hits" : [
      {
        "_index" : "atguigu",
        "_type" : "goods",
        "_id" : "1",
        "_score" : 1.0,
        "_source" : {
          "title" : "华为手机",
          "images" : "http://image.jd.com/12479122.jpg",
          "price" : 4288
        }
      },
      {
        "_index" : "atguigu",
        "_type" : "goods",
        "_id" : "2",
        "_score" : 1.0,
        "_source" : {
          "title" : "小米手机",
          "images" : "http://image.jd.com/12479122.jpg",
          "price" : 2899,
          "stock" : 200,
          "saleable" : true,
          "attr" : {
            "category" : "手机",
            "brand" : "小米"
          }
        }
      }
    ]
  }
}
```

再看下索引库的映射关系: `GET /atguigu/_mapping`

```json
{
  "atguigu" : {
    "mappings" : {
      "goods" : {
        "properties" : {
          "attr" : {
            "properties" : {
              "brand" : {
                "type" : "text",
                "fields" : {
                  "keyword" : {
                    "type" : "keyword",
                    "ignore_above" : 256
                  }
                }
              },
              "category" : {
                "type" : "text",
                "fields" : {
                  "keyword" : {
                    "type" : "keyword",
                    "ignore_above" : 256
                  }
                }
              }
            }
          },
          "images" : {
            "type" : "keyword",
            "index" : false
          },
          "price" : {
            "type" : "long"
          },
          "saleable" : {
            "type" : "boolean"
          },
          "stock" : {
            "type" : "long"
          },
          "title" : {
            "type" : "text",
            "analyzer" : "ik_max_word"
          }
        }
      }
    }
  }
}
```

stock，saleable，attr都被成功映射了。

如果是字符串类型的数据，会添加两种类型：text + keyword。如上例中的category 和 brand

### 2.4.4.整体覆盖

把刚才新增的请求方式改为PUT。不过必须指定id，

- id对应文档存在，则修改
- id对应文档不存在，则新增

比如，我们把id为2的数据进行修改：

```json
PUT /atguigu/goods/2
{
    "title":"超米手机",
    "images":"http://image.jd.com/12479122.jpg",
    "price":2999,
    "stock": 200,
    "saleable":true,
    "attr": {
        "category": "手机",
        "brand": "小米"
    }
}
```

结果：`GET /atguigu/goods/2`

```json
{
  "_index" : "atguigu",
  "_type" : "goods",
  "_id" : "2",
  "_version" : 7,
  "_seq_no" : 7,
  "_primary_term" : 1,
  "found" : true,
  "_source" : {
    "title" : "超米手机",
    "images" : "http://image.jd.com/12479122.jpg",
    "price" : 2899,
    "stock" : 200,
    "saleable" : true,
    "attr" : {
      "category" : "手机",
      "brand" : "小米"
    }
  }
}
```

**这种方式必须有所有字段，否则会导致更新后的数据字段缺失。**

### 2.4.5.更新字段

语法：

```
POST /{index}/{type}/{id}/_update
{
	"doc": {
		字段名: 字段值
	}
}
```

演示：

```json
POST /atguigu/goods/2/_update
{
  "doc": {
    "price":1999
  }
}
```

### 2.4.6.删除数据

删除使用DELETE请求，同样，需要根据id进行删除：

> 语法

```
DELETE /索引库名/类型名/id值
```

> 示例：

```
DELETE /atguigu/goods/3
```

> 结果：

```json
{
    "_index" : "atguigu",
    "_type" : "goods",
    "_id" : "3",
    "_version" : 2,
    "result" : "deleted",
    "_shards" : {
        "total" : 4,
        "successful" : 1,
        "failed" : 0
    },
    "_seq_no" : 1,
    "_primary_term" : 1
}
```



# 3.es查询

基本查询语法如下：

```json
GET /索引库名/_search
{
    "query":{
        "查询类型":{
            "查询条件":"查询条件值"
        }
    }
}
```

这里的query代表一个查询对象，里面可以有不同的查询属性

- 查询类型：
  - 例如：`match_all`， `match`，`term` ， `range` 等等
- 查询条件：查询条件会根据类型的不同，写法也有差异，后面详细讲解

查询结果：

- took：查询花费时间，单位是毫秒
- time_out：是否超时
- _shards：分片信息
- hits：搜索结果总览对象
  - total：搜索到的总条数
  - max_score：所有结果中文档得分的最高分
  - hits：搜索结果的文档对象数组，每个元素是一条搜索到的文档信息
    - _index：索引库
    - _type：文档类型
    - _id：文档id
    - _score：文档得分
    - _source：文档的源数据

## 3.1.数据准备

> 批量插入

```json
POST /atguigu/goods/_bulk
{"index":{"_id":1}}
{ "title":"小米手机", "images":"http://image.jd.com/12479122.jpg", "price":1999, "stock": 200, "attr": { "category": "手机", "brand": "小米" } }
{"index":{"_id":2}}
{"title":"超米手机", "images":"http://image.jd.com/12479122.jpg", "price":2999, "stock": 300, "attr": { "category": "手机", "brand": "小米" } }
{"index":{"_id":3}}
{ "title":"小米电视", "images":"http://image.jd.com/12479122.jpg", "price":3999, "stock": 400, "attr": { "category": "电视", "brand": "小米" } }
{"index":{"_id":4}}
{ "title":"小米笔记本", "images":"http://image.jd.com/12479122.jpg", "price":4999, "stock": 200, "attr": { "category": "笔记本", "brand": "小米" } }
{"index":{"_id":5}}
{ "title":"华为手机", "images":"http://image.jd.com/12479122.jpg", "price":3999, "stock": 400, "attr": { "category": "手机", "brand": "华为" } }
{"index":{"_id":6}}
{ "title":"华为笔记本", "images":"http://image.jd.com/12479122.jpg", "price":5999, "stock": 200, "attr": { "category": "笔记本", "brand": "华为" } }
{"index":{"_id":7}}
{ "title":"荣耀手机", "images":"http://image.jd.com/12479122.jpg", "price":2999, "stock": 300, "attr": { "category": "手机", "brand": "华为" } }
{"index":{"_id":8}}
{ "title":"oppo手机", "images":"http://image.jd.com/12479122.jpg", "price":2799, "stock": 400, "attr": { "category": "手机", "brand": "oppo" } }
{"index":{"_id":9}}
{ "title":"vivo手机", "images":"http://image.jd.com/12479122.jpg", "price":2699, "stock": 300, "attr": { "category": "手机", "brand": "vivo" } }
{"index":{"_id":10}}
{ "title":"华为nova手机", "images":"http://image.jd.com/12479122.jpg", "price":2999, "stock": 300, "attr": { "category": "手机", "brand": "华为" } }
```

## 3.2.匹配查询（match）

> 匹配所有

```json
GET /atguigu/_search
{
    "query": {
        "match_all": {}
    }
}
```

- `query`：代表查询对象
- `match_all`：代表查询所有

> 条件匹配

```json
GET /atguigu/_search
{
    "query": {
        "match": {
            "title": "小米手机"
        }
    }
}
```

查询出很多数据，不仅包括`小米手机`，而且与`小米`或者`手机`相关的都会查询到，说明多个词之间是`or`的关系。

某些情况下，我们需要更精确查找，我们希望这个关系变成`and`，可以这样做：

```json
GET /atguigu/_search
{
    "query": {
        "match": {
            "title": {
                "query": "小米手机",
                "operator": "and"
            }
        }
    }
}
```

查询结果：

```json
{
    "took" : 26,
    "timed_out" : false,
    "_shards" : {
        "total" : 2,
        "successful" : 2,
        "skipped" : 0,
        "failed" : 0
    },
    "hits" : {
        "total" : 1,
        "max_score" : 1.7037868,
        "hits" : [
            {
                "_index" : "atguigu",
                "_type" : "goods",
                "_id" : "1",
                "_score" : 1.7037868,
                "_source" : {
                    "title" : "小米手机",
                    "images" : "http://image.jd.com/12479122.jpg",
                    "price" : 1999,
                    "stock" : 200,
                    "attr" : {
                        "category" : "手机",
                        "brand" : "小米"
                    }
                }
            }
        ]
    }
}
```

> 嵌套字段匹配

```json
GET /atguigu/_search
{
    "query": {
        "match": {
            "attr.brand": "小米"
        }
    }
}
```

> 短句匹配

按短语查询，不再利用分词技术，直接用短语在原始数据中匹配

```json
GET /atguigu/_search
{
    "query": {
        "match_phrase": {
            "title": "小米手机"
        }
    }
}
```

> 多字段匹配

`match`只能根据一个字段匹配查询，如果要根据多个字段匹配查询可以使用`multi_match`

```json
GET /atguigu/_search
{
    "query": {
        "multi_match": {
            "query": "小米",
            "fields": ["title", "attr.brand.keyword"]
        }
    }
}
```

## 3.3.词条查询（term）

`term` 查询被用于精确值匹配，这些精确值可能是数字、时间、布尔或者那些**未分词**的字符串。

```json
GET /atguigu/_search
{
    "query": {
        "term": {
            "price": 4999
        }
    }
}
```

> 多词条查询

`terms` 查询和 `term` 查询一样，但它允许你指定多值进行匹配。如果这个字段包含了指定值中的任何一个值，那么这个文档满足条件：

```json
GET /atguigu/_search
{
    "query": {
        "terms": {
            "price": [2699, 3999]
        }
    }
}
```

## 3.4.范围查询（range）

`range` 查询找出那些落在指定区间内的数字或者时间

```json
GET /atguigu/_search
{
    "query":{
        "range": {
            "price": {
                "gte":  1000,
                "lt":   3000
            }
        }
    }
}
```

`range`查询允许以下字符：

| 操作符 |   说明   |
| :----: | :------: |
|   gt   |   大于   |
|  gte   | 大于等于 |
|   lt   |   小于   |
|  lte   | 小于等于 |

## 3.5.模糊查询（fuzzy）

`fuzzy` 允许用户搜索词条与实际词条的拼写出现偏差，但是偏差的编辑距离不得超过2：

```json
GET /atguigu/_search
{
    "query": {
        "fuzzy": {
            "title": "oppe"
        }
    }
}
```

上面的查询，也能查询到apple手机

可以通过`fuzziness`来指定允许的编辑距离：

```json
GET /atguigu/_search
{
    "query": {
        "fuzzy": {
            "title": {
                "value": "oppe",
                "fuzziness": 1
            }
        }
    }
}
```

**编辑距离：从错误的词到正确词条需要修改的次数。例如：oppe--->oppo，需要修改一次，编辑距离就是1。**

**elasticsearch支持的最大编辑距离是2。**

## 3.6.布尔组合（bool）

布尔查询又叫**组合查询**

`bool`把各种其它查询通过`must`（与）、`must_not`（非）、`should`（或）的方式进行组合

```json
GET /atguigu/_search
{
    "query": {
        "bool": {
            "must": [
                {
                    "range": {
                        "price": {
                            "gte": 1000,
                            "lte": 3000
                        }
                    }
                },
                {
                    "range": {
                        "price": {
                            "gte": 2000,
                            "lte": 4000
                        }
                    }
                }
            ]
        }
    }
}
```

注意：一个组合查询里面只能出现一种组合，不能混用

## 3.7.过滤（filter）

所有的查询都会影响到文档的评分及排名。如果我们需要在查询结果中进行过滤，并且不希望过滤条件影响评分，那么就不要把过滤条件作为查询条件来用。而是使用`filter`方式：

```json
GET /atguigu/_search
{
    "query": {
        "bool": {
            "must": {
                "match": { 
                    "title": "小米手机"
                }
            },
            "filter": {
                "range": {
                    "price": {
                        "gt": 2000,
                        "lt": 3000
                    }
                }
            }
        }
    }
}
```

注意：`filter`中还可以再次进行`bool`组合条件过滤。

## 3.8.排序（sort）

`sort` 可以让我们按照不同的字段进行排序，并且通过`order`指定排序的方式

```json
GET /atguigu/_search
{
    "query": {
        "match": {
            "title": "小米手机"
        }
    },
    "sort": [
        {
            "price": { 
                "order": "desc"
            }
        },
        {
            "_score": {
                "order": "desc"
            }
        }
    ]
}
```

## 3.9.分页（from/size）

`from`：从那一条开始

`size`：取多少条

```json
GET /atguigu/_search
{
    "query": {
        "match": {
            "title": "小米手机"
        }
    },
    "from": 2,
    "size": 2
}
```

## 3.10.高亮（highlight）

查看百度高亮的原理：

![1563258499361](assets/1563258499361-1585450087261.png)

发现：高亮的本质是给关键字添加了<em>标签，在前端再给该标签添加样式即可。

```json
GET /atguigu/_search
{
    "query": {
        "match": {
            "title": "小米"
        }
    },
    "highlight": {
        "fields": {
            "title": {}
        }, 
        "pre_tags": "<em>",
        "post_tags": "</em>"
    }
}
```

`fields`：高亮字段

`pre_tags`：前置标签

`post_tags`：后置标签

查询结果如下：

![1563258748370](assets/1563258748370-1585450087260.png)

## 3.11.结果过滤（_source）

默认情况下，elasticsearch在搜索的结果中，会把文档中保存在`_source`的所有字段都返回。

如果我们只想获取其中的部分字段，可以添加`_source`的过滤

```json
GET /atguigu/_search
{
    "_source": ["title", "price"],
    "query": {
        "term": {
            "price": 2699
        }
    }
}
```

返回结果，只有两个字段：

```json
{
    "took" : 9,
    "timed_out" : false,
    "_shards" : {
        "total" : 2,
        "successful" : 2,
        "skipped" : 0,
        "failed" : 0
    },
    "hits" : {
        "total" : 1,
        "max_score" : 1.0,
        "hits" : [
            {
                "_index" : "atguigu",
                "_type" : "goods",
                "_id" : "9",
                "_score" : 1.0,
                "_source" : {
                    "price" : 2699,
                    "title" : "vivo手机"
                }
            }
        ]
    }
}
```



# 4.es聚合

Elasticsearch中的聚合，包含多种类型，最常用的两种，一个叫`桶`，一个叫`度量`：

> 桶（bucket）

桶的作用，是按照某种方式对数据进行分组，每一组数据在ES中称为一个`桶`，例如我们根据国籍对人划分，可以得到`中国桶`、`英国桶`，`日本桶`……或者我们按照年龄段对人进行划分：0~10,10~20,20~30,30~40等。

Elasticsearch中提供的划分桶的方式有很多：

- Date Histogram Aggregation：根据日期阶梯分组，例如给定阶梯为周，会自动每周分为一组
- Histogram Aggregation：根据数值阶梯分组，与日期类似
- Terms Aggregation：根据词条内容分组，词条内容完全匹配的为一组
- Range Aggregation：数值和日期的范围分组，指定开始和结束，然后按段分组
- ……

bucket aggregations 只负责对数据进行分组，并不进行计算，因此往往bucket中往往会嵌套另一种聚合：metrics aggregations即度量

> 度量（metrics）

分组完成以后，我们一般会对组中的数据进行聚合运算，例如求平均值、最大、最小、求和等，这些在ES中称为`度量`

比较常用的一些度量聚合方式：

- Avg Aggregation：求平均值
- Max Aggregation：求最大值
- Min Aggregation：求最小值
- Percentiles Aggregation：求百分比
- Stats Aggregation：同时返回avg、max、min、sum、count等
- Sum Aggregation：求和
- Top hits Aggregation：求前几
- Value Count Aggregation：求总数
- ……

## 4.1.聚合为桶

首先，我们按照手机的品牌`attr.brand.keyword`来划分`桶`

```json
GET /atguigu/_search
{
    "size": 0,
    "aggs": { 
        "brands": { 
            "terms": { 
                "field": "attr.brand.keyword"
            }
        }
    }
}
```

- size： 查询条数，这里设置为0，因为我们不关心搜索到的数据，只关心聚合结果，提高效率
- aggs：声明这是一个聚合查询，是aggregations的缩写
  - brands：给这次聚合起一个名字，任意。
    - terms：划分桶的方式，这里是根据词条划分
      - field：划分桶的字段

结果：

```json
{
    "took" : 124,
    "timed_out" : false,
    "_shards" : {
        "total" : 2,
        "successful" : 2,
        "skipped" : 0,
        "failed" : 0
    },
    "hits" : {
        "total" : 10,
        "max_score" : 0.0,
        "hits" : [ ]
    },
    "aggregations" : {
        "brands" : {
            "doc_count_error_upper_bound" : 0,
            "sum_other_doc_count" : 0,
            "buckets" : [
                {
                    "key" : "华为",
                    "doc_count" : 4
                },
                {
                    "key" : "小米",
                    "doc_count" : 4
                },
                {
                    "key" : "oppo",
                    "doc_count" : 1
                },
                {
                    "key" : "vivo",
                    "doc_count" : 1
                }
            ]
        }
    }
}
```

- hits：查询结果为空，因为我们设置了size为0
- aggregations：聚合的结果
- brands：我们定义的聚合名称
- buckets：查找到的桶，每个不同的品牌字段值都会形成一个桶
  - key：这个桶对应的品牌字段的值
  - doc_count：这个桶中的文档数量

## 4.2.桶内度量

前面的例子告诉我们每个桶里面的文档数量，这很有用。 但通常，我们的应用需要提供更复杂的文档度量。 例如，每种品牌手机的平均价格是多少？

因此，我们需要告诉Elasticsearch`使用哪个字段`，`使用何种度量方式`进行运算，这些信息要嵌套在`桶`内，`度量`的运算会基于`桶`内的文档进行现在，我们为刚刚的聚合结果添加 求价格平均值的度量：

```json
GET /atguigu/_search
{
    "size" : 0,
    "aggs" : { 
        "brands" : { 
            "terms" : { 
                "field" : "attr.brand.keyword"
            },
            "aggs": {
                "avg_price": { 
                    "avg": {
                        "field": "price" 
                    }
                }
            }
        }
    }
}
```

- aggs：我们在上一个aggs(brands)中添加新的aggs。可见`度量`也是一个聚合
- avg_price：聚合的名称
- avg：度量的类型，这里是求平均值
- field：度量运算的字段

结果：

```json
{
    "took" : 41,
    "timed_out" : false,
    "_shards" : {
        "total" : 2,
        "successful" : 2,
        "skipped" : 0,
        "failed" : 0
    },
    "hits" : {
        "total" : 10,
        "max_score" : 0.0,
        "hits" : [ ]
    },
    "aggregations" : {
        "brands" : {
            "doc_count_error_upper_bound" : 0,
            "sum_other_doc_count" : 0,
            "buckets" : [
                {
                    "key" : "华为",
                    "doc_count" : 4,
                    "avg_price" : {
                        "value" : 3999.0
                    }
                },
                {
                    "key" : "小米",
                    "doc_count" : 4,
                    "avg_price" : {
                        "value" : 3499.0
                    }
                },
                {
                    "key" : "oppo",
                    "doc_count" : 1,
                    "avg_price" : {
                        "value" : 2799.0
                    }
                },
                {
                    "key" : "vivo",
                    "doc_count" : 1,
                    "avg_price" : {
                        "value" : 2699.0
                    }
                }
            ]
        }
    }
}
```

可以看到每个桶中都有自己的`avg_price`字段，这是度量聚合的结果

## 4.3.桶内嵌套桶

刚刚的案例中，我们在桶内嵌套度量运算。事实上桶不仅可以嵌套运算， 还可以再嵌套其它桶。也就是说在每个分组中，再分更多组。

比如：我们想统计每个品牌都生产了那些产品，按照`attr.category.keyword`字段再进行分桶

```json
GET /atguigu/_search
{
    "size" : 0,
    "aggs" : { 
        "brands" : { 
            "terms" : { 
                "field" : "attr.brand.keyword"
            },
            "aggs":{
                "avg_price": { 
                    "avg": {
                        "field": "price" 
                    }
                },
                "categorys": {
                    "terms": {
                        "field": "attr.category.keyword"
                    }
                }
            }
        }
    }
}
```

部分结果：

```json
{
    "took" : 19,
    "timed_out" : false,
    "_shards" : {
        "total" : 2,
        "successful" : 2,
        "skipped" : 0,
        "failed" : 0
    },
    "hits" : {
        "total" : 10,
        "max_score" : 0.0,
        "hits" : [ ]
    },
    "aggregations" : {
        "brands" : {
            "doc_count_error_upper_bound" : 0,
            "sum_other_doc_count" : 0,
            "buckets" : [
                {
                    "key" : "华为",
                    "doc_count" : 4,
                    "categorys" : {
                        "doc_count_error_upper_bound" : 0,
                        "sum_other_doc_count" : 0,
                        "buckets" : [
                            {
                                "key" : "手机",
                                "doc_count" : 3
                            },
                            {
                                "key" : "笔记本",
                                "doc_count" : 1
                            }
                        ]
                    },
                    "avg_price" : {
                        "value" : 3999.0
                    }
                },
                {
                    "key" : "小米",
                    "doc_count" : 4,
                    "categorys" : {
                        "doc_count_error_upper_bound" : 0,
                        "sum_other_doc_count" : 0,
                        "buckets" : [
                            {
                                "key" : "手机",
                                "doc_count" : 2
                            },
                            {
                                "key" : "电视",
                                "doc_count" : 1
                            },
                            {
                                "key" : "笔记本",
                                "doc_count" : 1
                            }
                        ]
                    },
                    "avg_price" : {
                        "value" : 3499.0
                    }
                },
                {
                    "key" : "oppo",
                    "doc_count" : 1,
                    "categorys" : {
                        "doc_count_error_upper_bound" : 0,
                        "sum_other_doc_count" : 0,
                        "buckets" : [
                            {
                                "key" : "手机",
                                "doc_count" : 1
                            }
                        ]
                    },
                    "avg_price" : {
                        "value" : 2799.0
                    }
                },
                {
                    "key" : "vivo",
                    "doc_count" : 1,
                    "categorys" : {
                        "doc_count_error_upper_bound" : 0,
                        "sum_other_doc_count" : 0,
                        "buckets" : [
                            {
                                "key" : "手机",
                                "doc_count" : 1
                            }
                        ]
                    },
                    "avg_price" : {
                        "value" : 2699.0
                    }
                }
            ]
        }
    }
}
```

- 我们可以看到，新的聚合`categorys`被嵌套在原来每一个`brands`的桶中。
- 每个品牌下面都根据 `attr.category.keyword`字段进行了分组
- 我们能读取到的信息：
  - 华为有4中产品
  - 华为产品的平均售价是 3999.0美元。
  - 其中3种手机产品，1种笔记本产品



# 5.SpringDataElasticsearch

