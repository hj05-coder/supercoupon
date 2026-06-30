# SuperCoupon — 超级优惠券系统

基于 **Spring Cloud 微服务架构**的高性能优惠券管理平台，支持高并发兑换/秒杀、批量分发、订单结算、预约提醒等核心业务场景。

## 🏗 项目架构

```
supercoupon-all (父 POM)
├── gateway          # 网关服务 :10000 — 请求路由、日志、限流
├── merchant-admin   # 商家管理后台 :10010 — 优惠券模板 & 分发任务管理
├── engine           # 核心引擎 :10020 — 兑换/秒杀、锁定/核销/退款、预约提醒
├── settlement       # 结算服务 :10030 — 优惠券查询、折扣金额计算
├── distribution     # 分发服务 :10040 — 批量分发优惠券、EasyExcel 解析
├── search           # 搜索服务 :10050 — 基于 Elasticsearch 的优惠券搜索
└── framework        # 公共框架 — 异常处理、幂等性、缓存配置
```

## 🚀 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 基础框架 | Spring Boot / Spring Cloud / Spring Cloud Alibaba | 3.0.7 / 2022.0.3 / 2022.0.0.0-RC2 |
| ORM | MyBatis-Plus | 3.5.7 |
| 分库分表 | Apache ShardingSphere | 5.3.2 |
| 消息队列 | Apache RocketMQ | 2.3.0 |
| 缓存 & 分布式锁 | Redis + Redisson | 3.27.2 |
| 分布式任务调度 | XXL-Job | 2.4.1 |
| Excel 处理 | EasyExcel | 4.0.1 |
| 搜索引擎 | Elasticsearch | 2.6.12 |
| 服务注册发现 | Nacos | — |
| API 文档 | Knife4j (Swagger) | 4.5.0 |
| 操作日志 | BizLog SDK | 3.0.6 |
| 工具库 | Guava / Hutool / Fastjson2 / Dozer / Lombok | — |

## ✨ 核心功能

### 1. 优惠券模板管理 (`merchant-admin`)
- 商家创建/查询/终止优惠券模板
- 分页查询优惠券模板列表
- 增加优惠券模板发行量（防重复提交）
- 优惠券模板操作日志（BizLog + SpEL 解析）

### 2. 优惠券兑换/秒杀 (`engine`)
- 高并发场景下用户兑换优惠券（Redis 库存扣减 + 异步削峰）
- 基于 RocketMQ 消息队列的异步兑换，应对秒杀流量
- 布隆过滤器 + Redis 缓存空值解决缓存穿透/击穿

### 3. 优惠券批量分发 (`distribution`)
- 批量分发优惠券给指定用户
- EasyExcel 百万级用户数据导入解析
- 分发失败记录兜底策略及深分页优化
- XXL-Job 定时扫描待执行任务并触发分发

### 4. 优惠券生命周期管理 (`engine`)
- **锁定** — 用户下单时锁定使用的优惠券（创建结算单）
- **核销** — 用户支付成功后核销优惠券
- **退款** — 用户退款时退还优惠券

### 5. 优惠券结算 (`settlement`)
- 查询用户可用/不可用优惠券列表（并发查询优化）
- 多类型优惠券折扣金额计算，使用策略模式：
  - 折扣券 (`DiscountCouponStrategy`)
  - 满减券 (`ThresholdCouponStrategy`)
  - 固定减免券 (`FixedDiscountCouponStrategy`)

### 6. 优惠券预约提醒 (`engine`)
- 创建/取消优惠券预约提醒
- 定时发送预约通知（App 推送 / 邮件通知）
- 基于策略模式的多种提醒方式

### 7. 幂等性保障 (`framework`)
- **接口幂等** — `@NoDuplicateSubmit` 注解防重复提交
- **消息幂等** — `@NoMQDuplicateConsume` 注解 + 去重表防消息重复消费
- 基于 Redis 分布式锁 + SpEL 表达式组合 key

### 8. 数据分片 (`framework`)
- 基于 ShardingSphere 的 Hash 取模分库分表算法
- 用户优惠券表 / 优惠券结算表等按用户 ID 水平分片

## 📁 项目结构

```
supercoupon/
├── gateway/                    # 网关模块
│   └── src/main/java/.../gateway/
│       ├── GatewayApplication.java
│       └── filter/RequestLoggingFilter.java
│
├── merchant-admin/             # 商家管理后台
│   └── src/main/java/.../merchant/admin/
│       ├── controller/         # CouponTemplateController, CouponTaskController
│       ├── service/            # 业务逻辑 + 责任链过滤器
│       │   ├── impl/           # CouponTemplateServiceImpl, CouponTaskServiceImpl
│       │   └── handler/filter/ # 模板创建参数校验责任链
│       ├── dao/                # entity + mapper + sharding 算法
│       ├── dto/req/, dto/resp/ # 请求/响应 DTO
│       ├── mq/                 # 生产者/消费者/事件
│       ├── job/                # XXL-Job 定时任务处理器
│       └── config/             # 数据库/布隆过滤器/Swagger/XXL-Job 配置
│
├── distribution/               # 分发服务
│   └── src/main/java/.../distribution/
│       ├── service/handler/excel/  # EasyExcel 读取监听器
│       ├── mq/consumer/            # 优惠券执行分发消费者
│       └── dao/                    # CouponTask 相关 Mapper
│
├── engine/                     # 核心引擎
│   └── src/main/java/.../engine/
│       ├── controller/         # CouponTemplateController, UserCouponController, RemindController
│       ├── service/            # 优惠券模板/用户优惠券/提醒服务
│       │   └── handler/remind/ # 提醒策略（App推送/邮件）
│       ├── mq/consumer/        # Canal Binlog 同步 / 延迟关闭 / 兑换 / 提醒延时消费
│       ├── dao/                # 优惠券模板/用户优惠券/结算/Mapper
│       └── toolkit/            # Redis Lua 脚本库存扣减工具
│
├── settlement/                 # 结算服务
│   └── src/main/java/.../settlement/
│       ├── controller/         # CouponQueryController
│       ├── service/
│       │   ├── impl/           # CouponQueryServiceImpl
│       │   └── strategy/       # 折扣计算策略（折扣/满减/固定减免）
│       └── toolkit/            # 优惠券工厂
│
├── search/                     # 搜索服务（Elasticsearch）
├── framework/                  # 公共框架
│   └── src/main/java/.../framework/
│       ├── config/             # 缓存/幂等性/Web/Redis 自动装配
│       ├── idempotent/         # @NoDuplicateSubmit / @NoMQDuplicateConsume 注解+AOP
│       ├── exception/          # 自定义异常体系
│       ├── errorcode/          # 错误码定义
│       ├── result/             # 统一返回结果 Result<T>
│       └── web/                # 全局异常处理器
│
└── pom.xml                     # 父 POM（依赖版本管理）
```

## 🔧 快速开始

### 环境要求

- **JDK** 17+
- **Maven** 3.6+
- **MySQL** 8.0+
- **Redis** 6.0+
- **RocketMQ** 4.9+
- **Elasticsearch** 7.x（搜索服务）
- **Nacos** 2.x（服务注册发现，可选）
- **XXL-Job Admin**（定时任务调度，可选）

### 本地运行

1. **克隆项目**
   ```bash
   git clone <repo-url>
   cd supercoupon
   ```

2. **配置数据库**
   - 创建 MySQL 数据库及分片表
   - 修改各模块 `application.yaml` 中的数据库连接信息
   - 调整 `shardingsphere-config.yaml` 中的分片规则

3. **启动基础设施**
   ```bash
   # 启动 Redis
   redis-server

   # 启动 RocketMQ NameServer & Broker
   nohup sh bin/mqnamesrv &
   nohup sh bin/mqbroker -n localhost:9876 &

   # 启动 XXL-Job Admin（如需要）
   # 启动 Nacos（如需要）
   ```

4. **编译项目**
   ```bash
   mvn clean compile -DskipTests
   ```

5. **启动服务（按顺序）**
   ```bash
   # 端口规划：
   # gateway        : 10000  (网关)
   # merchant-admin : 10010  (商家后台)
   # engine         : 10020  (核心引擎)
   # settlement     : 10030  (结算服务)
   # distribution   : 10040  (分发服务)
   # search         : 10050  (搜索服务)

   # 依次启动各模块 Application 主类
   # - GatewayApplication
   # - MerchantAdminApplication
   # - EngineApplication
   # - SettlementApplication
   # - DistributionApplication
   # - SearchApplication
   ```

6. **访问 API 文档**
   - Knife4j Swagger: `http://localhost:{port}/swagger-ui.html`
   - 示例: `http://localhost:10010/swagger-ui.html`

### 运行测试

```bash
mvn test
```

测试覆盖内容：
- 优惠券模板创建并发/幂等性测试
- 优惠券数量并发递增测试
- SpEL 日志表达式解析测试
- Mock 数据生成与验证测试
- EasyExcel 文件生成测试

## 🗄 数据库设计

### 分库分表规划

| 数据库 | 用途 |
|--------|------|
| `one_coupon_rebuild_0` | 分片库 0（engine / merchant-admin / distribution） |
| `one_coupon_rebuild_1` | 分片库 1（engine / merchant-admin / distribution） |
| `one_coupon_0` | 分片库 0（settlement） |
| `one_coupon_1` | 分片库 1（settlement） |

| 逻辑表 | 物理分片 | 分片键 | 说明 |
|--------|----------|--------|------|
| `t_coupon_template` | 2 库 × 16 表 = 32 片 | `shop_number` | 优惠券模板 |
| `t_user_coupon` | 2 库 × 32 表 = 64 片 | `user_id` | 用户优惠券 |
| `t_coupon_settlement` | 2 库 × 8~16 表 | `user_id` | 优惠券结算单 |
| `t_coupon_task` | 不分片（单表） | — | 分发任务 |
| `t_coupon_task_fail` | 不分片（单表） | — | 分发失败记录 |
| `t_coupon_template_remind` | 不分片（单表） | — | 预约提醒 |
| `t_coupon_template_log` | 不分片（单表） | — | 模板操作日志 |

### 建表 DDL

> 执行前请将下面的 `${db}` 替换为实际数据库名，`${N}` 替换为分片编号。

#### 基础数据库 & 物理表创建脚本

由于 ShardingSphere 管理大量分片表（最多 64 张 `t_user_coupon`），推荐使用以下脚本自动生成并执行 DDL。

<details>
<summary><b>📜 一键建表脚本 (Linux/macOS)</b></summary>

```bash
#!/bin/bash
# 创建所需数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS one_coupon_rebuild_0 DEFAULT CHARSET utf8mb4;"
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS one_coupon_rebuild_1 DEFAULT CHARSET utf8mb4;"
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS one_coupon_0 DEFAULT CHARSET utf8mb4;"
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS one_coupon_1 DEFAULT CHARSET utf8mb4;"

# 在 one_coupon_rebuild_0 & one_coupon_rebuild_1 中创建 t_coupon_template_{0..15}
for DB in one_coupon_rebuild_0 one_coupon_rebuild_1; do
  for i in $(seq 0 15); do
    mysql -u root -p "$DB" -e "
      CREATE TABLE IF NOT EXISTS t_coupon_template_$i (
        id BIGINT NOT NULL AUTO_INCREMENT,
        shop_number BIGINT NOT NULL COMMENT '店铺编号',
        name VARCHAR(256) DEFAULT NULL COMMENT '优惠券名称',
        source INT DEFAULT NULL COMMENT '优惠券来源 0:店铺券 1:平台券',
        target INT DEFAULT NULL COMMENT '优惠对象 0:商品专属 1:全店通用',
        goods VARCHAR(1024) DEFAULT NULL COMMENT '优惠商品编码',
        type INT DEFAULT NULL COMMENT '优惠类型 0:立减券 1:满减券 2:折扣券',
        valid_start_time DATETIME DEFAULT NULL COMMENT '有效期开始时间',
        valid_end_time DATETIME DEFAULT NULL COMMENT '有效期结束时间',
        stock INT DEFAULT NULL COMMENT '库存',
        receive_rule VARCHAR(1024) DEFAULT NULL COMMENT '领取规则',
        consume_rule VARCHAR(1024) DEFAULT NULL COMMENT '消耗规则',
        status INT DEFAULT NULL COMMENT '状态 0:生效中 1:已结束',
        create_time DATETIME DEFAULT NULL COMMENT '创建时间',
        update_time DATETIME DEFAULT NULL COMMENT '修改时间',
        del_flag INT DEFAULT NULL COMMENT '删除标识 0:未删除 1:已删除',
        PRIMARY KEY (id)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';
    "
  done

  # t_user_coupon_{0..31}
  for i in $(seq 0 31); do
    mysql -u root -p "$DB" -e "
      CREATE TABLE IF NOT EXISTS t_user_coupon_$i (
        id BIGINT NOT NULL AUTO_INCREMENT,
        user_id BIGINT NOT NULL COMMENT '用户ID',
        coupon_template_id BIGINT NOT NULL COMMENT '优惠券模板ID',
        receive_time DATETIME DEFAULT NULL COMMENT '领取时间',
        receive_count INT DEFAULT NULL COMMENT '领取次数',
        valid_start_time DATETIME DEFAULT NULL COMMENT '有效期开始时间',
        valid_end_time DATETIME DEFAULT NULL COMMENT '有效期结束时间',
        use_time DATETIME DEFAULT NULL COMMENT '使用时间',
        source INT DEFAULT NULL COMMENT '券来源 0:领券中心 1:平台发放 2:店铺领取',
        status INT DEFAULT NULL COMMENT '状态 0:未使用 1:锁定 2:已使用 3:已过期 4:已撤回',
        create_time DATETIME DEFAULT NULL COMMENT '创建时间',
        update_time DATETIME DEFAULT NULL COMMENT '修改时间',
        del_flag INT DEFAULT NULL COMMENT '删除标识 0:未删除 1:已删除',
        PRIMARY KEY (id)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';
    "
  done

  # t_coupon_settlement_{0..15}
  for i in $(seq 0 15); do
    mysql -u root -p "$DB" -e "
      CREATE TABLE IF NOT EXISTS t_coupon_settlement_$i (
        id BIGINT NOT NULL AUTO_INCREMENT,
        order_id BIGINT DEFAULT NULL COMMENT '订单ID',
        user_id BIGINT DEFAULT NULL COMMENT '用户ID',
        coupon_id BIGINT DEFAULT NULL COMMENT '优惠券ID',
        status INT DEFAULT NULL COMMENT '结算单状态 0:锁定 1:已取消 2:已支付 3:已退款',
        create_time DATETIME DEFAULT NULL COMMENT '创建时间',
        update_time DATETIME DEFAULT NULL COMMENT '修改时间',
        PRIMARY KEY (id)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券结算表';
    "
  done

  # 不分片表（只建一次，放在 rebuild_0 库中）
  if [ "$DB" = "one_coupon_rebuild_0" ]; then
    mysql -u root -p "$DB" -e "
      CREATE TABLE IF NOT EXISTS t_coupon_task (
        id BIGINT NOT NULL AUTO_INCREMENT,
        shop_number BIGINT DEFAULT NULL COMMENT '店铺编号',
        batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
        task_name VARCHAR(256) DEFAULT NULL COMMENT '批次任务名称',
        file_address VARCHAR(512) DEFAULT NULL COMMENT '文件地址',
        fail_file_address VARCHAR(512) DEFAULT NULL COMMENT '失败文件地址',
        send_num INT DEFAULT NULL COMMENT '发放优惠券数量',
        notify_type VARCHAR(256) DEFAULT NULL COMMENT '通知方式 0:站内信 1:弹框推送 2:邮箱 3:短信',
        coupon_template_id BIGINT DEFAULT NULL COMMENT '优惠券模板ID',
        send_type INT DEFAULT NULL COMMENT '发送类型 0:立即发送 1:定时发送',
        send_time DATETIME DEFAULT NULL COMMENT '发送时间',
        status INT DEFAULT NULL COMMENT '状态 0:待执行 1:执行中 2:执行失败 3:执行成功 4:取消',
        completion_time DATETIME DEFAULT NULL COMMENT '完成时间',
        operator_id BIGINT DEFAULT NULL COMMENT '操作人',
        create_time DATETIME DEFAULT NULL COMMENT '创建时间',
        update_time DATETIME DEFAULT NULL COMMENT '修改时间',
        del_flag INT DEFAULT NULL COMMENT '删除标识',
        PRIMARY KEY (id),
        INDEX idx_send_time_status (send_time, status)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券分发任务表';

      CREATE TABLE IF NOT EXISTS t_coupon_task_fail (
        id BIGINT NOT NULL AUTO_INCREMENT,
        batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
        json_object JSON COMMENT '失败原因JSON',
        PRIMARY KEY (id)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发失败记录表';

      CREATE TABLE IF NOT EXISTS t_coupon_template_remind (
        user_id BIGINT NOT NULL COMMENT '用户ID',
        coupon_template_id BIGINT NOT NULL COMMENT '优惠券模板ID',
        information BIGINT DEFAULT NULL COMMENT '预约信息位图',
        shop_number BIGINT DEFAULT NULL COMMENT '店铺编号',
        start_time DATETIME DEFAULT NULL COMMENT '开抢时间',
        PRIMARY KEY (user_id, coupon_template_id)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约提醒表';

      CREATE TABLE IF NOT EXISTS t_coupon_template_log (
        id BIGINT NOT NULL AUTO_INCREMENT,
        shop_number BIGINT DEFAULT NULL COMMENT '店铺编号',
        coupon_template_id VARCHAR(64) DEFAULT NULL COMMENT '模板ID',
        operator_id VARCHAR(64) DEFAULT NULL COMMENT '操作人ID',
        operation_log VARCHAR(2048) DEFAULT NULL COMMENT '操作日志',
        original_data TEXT COMMENT '原始数据',
        modified_data TEXT COMMENT '修改后数据',
        create_time DATETIME DEFAULT NULL COMMENT '创建时间',
        PRIMARY KEY (id)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板操作日志表';
    "
  fi
done

# settlement 模块独立数据库 one_coupon_0 & one_coupon_1: t_coupon_settlement_{0..7}
for DB in one_coupon_0 one_coupon_1; do
  for i in $(seq 0 7); do
    mysql -u root -p "$DB" -e "
      CREATE TABLE IF NOT EXISTS t_coupon_settlement_$i (
        id BIGINT NOT NULL AUTO_INCREMENT,
        order_id BIGINT DEFAULT NULL COMMENT '订单ID',
        user_id BIGINT DEFAULT NULL COMMENT '用户ID',
        coupon_id BIGINT DEFAULT NULL COMMENT '优惠券ID',
        status INT DEFAULT NULL COMMENT '结算单状态 0:锁定 1:已取消 2:已支付 3:已退款',
        create_time DATETIME DEFAULT NULL COMMENT '创建时间',
        update_time DATETIME DEFAULT NULL COMMENT '修改时间',
        PRIMARY KEY (id)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券结算表';
    "
  done
done

echo "✅ 所有数据库表创建完成！"
```
</details>

<details>
<summary><b>📜 手动建表 DDL（单表示例）</b></summary>

以下为各逻辑表对应的单张物理表 DDL，批量建表请替换表名后缀。

```sql
-- ============================================
-- 优惠券模板表 (t_coupon_template_0 ~ _15)
-- ============================================
CREATE TABLE IF NOT EXISTS t_coupon_template_0 (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    shop_number     BIGINT       NOT NULL COMMENT '店铺编号（分片键）',
    name            VARCHAR(256)          COMMENT '优惠券名称',
    source          INT                   COMMENT '来源 0:店铺券 1:平台券',
    target          INT                   COMMENT '优惠对象 0:商品专属 1:全店通用',
    goods           VARCHAR(1024)         COMMENT '优惠商品编码',
    type            INT                   COMMENT '类型 0:立减券 1:满减券 2:折扣券',
    valid_start_time DATETIME             COMMENT '有效期开始',
    valid_end_time  DATETIME              COMMENT '有效期结束',
    stock           INT                   COMMENT '库存',
    receive_rule    VARCHAR(1024)         COMMENT '领取规则(JSON)',
    consume_rule    VARCHAR(1024)         COMMENT '消耗规则(JSON)',
    status          INT                   COMMENT '状态 0:生效中 1:已结束',
    create_time     DATETIME              COMMENT '创建时间',
    update_time     DATETIME              COMMENT '修改时间',
    del_flag        INT                   COMMENT '删除标识 0:未删除 1:已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- ============================================
-- 用户优惠券表 (t_user_coupon_0 ~ _31)
-- ============================================
CREATE TABLE IF NOT EXISTS t_user_coupon_0 (
    id                  BIGINT   NOT NULL AUTO_INCREMENT,
    user_id             BIGINT   NOT NULL COMMENT '用户ID（分片键）',
    coupon_template_id  BIGINT   NOT NULL COMMENT '优惠券模板ID',
    receive_time        DATETIME          COMMENT '领取时间',
    receive_count       INT               COMMENT '领取次数',
    valid_start_time    DATETIME          COMMENT '有效期开始',
    valid_end_time      DATETIME          COMMENT '有效期结束',
    use_time            DATETIME          COMMENT '使用时间',
    source              INT               COMMENT '来源 0:领券中心 1:平台发放 2:店铺领取',
    status              INT               COMMENT '状态 0:未使用 1:锁定 2:已使用 3:已过期 4:已撤回',
    create_time         DATETIME          COMMENT '创建时间',
    update_time         DATETIME          COMMENT '修改时间',
    del_flag            INT               COMMENT '删除标识 0:未删除 1:已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ============================================
-- 优惠券结算表 (t_coupon_settlement_0 ~ _15 或 _7)
-- ============================================
CREATE TABLE IF NOT EXISTS t_coupon_settlement_0 (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    order_id    BIGINT            COMMENT '订单ID',
    user_id     BIGINT            COMMENT '用户ID（分片键）',
    coupon_id   BIGINT            COMMENT '优惠券ID',
    status      INT               COMMENT '状态 0:锁定 1:已取消 2:已支付 3:已退款',
    create_time DATETIME          COMMENT '创建时间',
    update_time DATETIME          COMMENT '修改时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券结算表';

-- ============================================
-- 不分片表（单表，建在 one_coupon_rebuild_0）
-- ============================================

-- 分发任务表
CREATE TABLE IF NOT EXISTS t_coupon_task (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    shop_number         BIGINT                COMMENT '店铺编号',
    batch_id            BIGINT                COMMENT '批次ID',
    task_name           VARCHAR(256)          COMMENT '批次任务名称',
    file_address        VARCHAR(512)          COMMENT 'Excel文件地址',
    fail_file_address   VARCHAR(512)          COMMENT '失败文件地址',
    send_num            INT                   COMMENT '发放数量',
    notify_type         VARCHAR(256)          COMMENT '通知方式 0:站内信 1:弹框推送 2:邮箱 3:短信',
    coupon_template_id  BIGINT                COMMENT '优惠券模板ID',
    send_type           INT                   COMMENT '发送类型 0:立即 1:定时',
    send_time           DATETIME              COMMENT '发送时间',
    status              INT                   COMMENT '状态 0:待执行 1:执行中 2:失败 3:成功 4:取消',
    completion_time     DATETIME              COMMENT '完成时间',
    operator_id         BIGINT                COMMENT '操作人',
    create_time         DATETIME              COMMENT '创建时间',
    update_time         DATETIME              COMMENT '修改时间',
    del_flag            INT                   COMMENT '删除标识',
    PRIMARY KEY (id),
    INDEX idx_send_time_status (send_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发任务表';

-- 分发失败记录表
CREATE TABLE IF NOT EXISTS t_coupon_task_fail (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    batch_id    BIGINT          COMMENT '批次ID',
    json_object JSON            COMMENT '失败详情JSON',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分发失败记录表';

-- 预约提醒表
CREATE TABLE IF NOT EXISTS t_coupon_template_remind (
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    coupon_template_id  BIGINT NOT NULL COMMENT '模板ID',
    information         BIGINT          COMMENT '预约信息（位图）',
    shop_number         BIGINT          COMMENT '店铺编号',
    start_time          DATETIME        COMMENT '开抢时间',
    PRIMARY KEY (user_id, coupon_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约提醒表';

-- 模板操作日志表
CREATE TABLE IF NOT EXISTS t_coupon_template_log (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    shop_number         BIGINT                COMMENT '店铺编号',
    coupon_template_id  VARCHAR(64)           COMMENT '模板ID',
    operator_id         VARCHAR(64)           COMMENT '操作人ID',
    operation_log       VARCHAR(2048)         COMMENT '操作日志',
    original_data       TEXT                  COMMENT '原始数据',
    modified_data       TEXT                  COMMENT '修改后数据',
    create_time         DATETIME              COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板操作日志表';
```
</details>

## 📊 核心业务流程

```
商家创建模板 ──→ 生成优惠券模板 ──→ 创建分发任务(定时/即时)
                                        │
                          ┌─────────────┴─────────────┐
                          ▼                           ▼
                    即时分发(MQ)              定时分发(XXL-Job)
                          │                           │
                          └─────────────┬─────────────┘
                                        ▼
                                  用户领取/兑换
                                        │
                          ┌─────────────┼─────────────┐
                          ▼             ▼             ▼
                      锁定优惠券    核销优惠券    退款优惠券
                      (下单时)     (支付后)     (退款后)
                          │             │             │
                          └─────────────┴─────────────┘
                                        │
                                        ▼
                              结算服务计算折扣金额
```

## 🛡 高并发 & 缓存策略

| 场景 | 策略 | 说明 |
|------|------|------|
| 秒杀兑换 | Redis 原子库存扣减 + RocketMQ 异步削峰 | Lua 脚本保证库存扣减原子性 |
| 缓存穿透 | 布隆过滤器 (Redisson RBloomFilter) | 过滤不存在的优惠券模板 ID |
| 缓存击穿 | Redis 缓存空值 + 互斥锁 | 防止热点数据过期瞬间击穿 DB |
| 缓存同步 | Canal Binlog 监听 + MQ | 数据库变更后同步更新缓存 |
| 消息幂等 | 去重表 + AOP 注解 | 基于 `msgId` 去重，防止重复消费 |
| 接口幂等 | Redis 分布式锁 + SpEL key | `@NoDuplicateSubmit` 注解防重复提交 |

## 🚢 部署指南

### 生产环境拓扑

```
                    ┌─────────────┐
                    │   Nginx      │  (反向代理 / 负载均衡)
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │   Gateway   │  :10000 (可多实例 + Nacos 负载)
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
   ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
   │ Merchant    │ │   Engine    │ │ Settlement  │
   │ Admin       │ │             │ │             │
   │ :10010      │ │ :10020      │ │ :10030      │
   └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
          │                │                │
   ┌──────▼──────┐ ┌──────▼──────┐         │
   │Distribution │ │   Search    │         │
   │ :10040      │ │ :10050      │         │
   └──────┬──────┘ └──────┬──────┘         │
          │                │                │
          └────────────────┼────────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
   ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
   │   MySQL     │ │   Redis     │ │  RocketMQ   │
   │ (1主2从)    │ │ (哨兵/集群) │ │ (2m-2s)     │
   └─────────────┘ └─────────────┘ └─────────────┘
```

### 中间件推荐配置

| 组件 | 生产配置 | 说明 |
|------|----------|------|
| MySQL | 1 主 2 从 + 读写分离 | 分片后可独立扩容 |
| Redis | Sentinel 哨兵 / Cluster 集群 | 缓存 + 分布式锁高可用 |
| RocketMQ | 2m-2s 异步复制 | 消息不丢 + 高吞吐 |
| Nacos | 3 节点集群 | 注册中心高可用 |
| XXL-Job | 独立部署 Admin | 定时任务统一管理 |
| Elasticsearch | 3 节点集群 | 搜索服务 |

### 打包部署

```bash
# 1. 打包所有模块
mvn clean package -DskipTests

# 2. 各模块 JAR 位置
# gateway/target/supercoupon-gateway.jar
# merchant-admin/target/supercoupon-merchant-admin.jar
# engine/target/supercoupon-engine.jar
# settlement/target/supercoupon-settlement.jar
# distribution/target/supercoupon-distribution.jar
# search/target/supercoupon-search.jar
```

### JVM 参数建议

```bash
# 网关 (轻量)
java -Xms512m -Xmx512m -jar gateway.jar

# 核心引擎 (高并发，内存适度上调)
java -Xms1g -Xmx1g -XX:+UseG1GC -jar engine.jar

# 分发/结算/搜索/商家后台
java -Xms512m -Xmx1g -XX:+UseG1GC -jar <module>.jar
```

### 环境变量覆盖

所有 `application.yaml` 中的配置均支持环境变量覆盖：

| 配置项 | 环境变量 | 默认值 |
|--------|----------|--------|
| 数据库地址 | `MYSQL_HOST` | 127.0.0.1 |
| 数据库端口 | `MYSQL_PORT` | 3306 |
| 数据库用户名 | `MYSQL_USER` | root |
| 数据库密码 | `MYSQL_PASSWORD` | — |
| Redis 地址 | `REDIS_HOST` | 127.0.0.1 |
| Redis 端口 | `REDIS_PORT` | 6379 |
| RocketMQ NameServer | `ROCKETMQ_NAMESRV` | 127.0.0.1:9876 |
| Nacos Server | `NACOS_SERVER` | 127.0.0.1:8848 |

### Docker Compose 一键启动（开发环境）

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: hj050101
    ports:
      - "3306:3306"
    volumes:
      - ./resources/database:/docker-entrypoint-initdb.d

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rocketmq-namesrv:
    image: apache/rocketmq:5.1.0
    command: sh mqnamesrv
    ports:
      - "9876:9876"

  rocketmq-broker:
    image: apache/rocketmq:5.1.0
    command: sh mqbroker -n rocketmq-namesrv:9876
    ports:
      - "10911:10911"
    depends_on:
      - rocketmq-namesrv

  elasticsearch:
    image: elasticsearch:7.17.10
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
```

> 启动中间件后，再按上面的顺序依次启动各 Java 服务模块。

## 📝 开发约定

- 代码风格：遵循阿里巴巴 Java 开发规范
- 分层架构：Controller → Service → Mapper (DAO)
- 统一返回：`Result<T>` 封装所有 API 返回值
- 异常处理：全局异常拦截器统一处理，禁止 try-catch 吞异常
- 错误码：`BaseErrorCode` 统一管理
- 分库分表：按 `userId` Hash 取模分片
- Commit 规范：遵循 Conventional Commits (`feat:` / `fix:` / `refactor:` / ...)

## 📄 License

Internal Project — All Rights Reserved

---

**Author:** web-cat / coder-cat
