# 麻将对局记录API接口测试指南

## 测试环境准备

### 1. 数据库服务启动

您需要确保PostgreSQL数据库服务正在运行：

```bash
# 确保本地PostgreSQL服务已启动，并创建数据库
psql -U postgres -c "CREATE DATABASE mahjong_db;"
```

### 2. 后端服务启动

```bash
# 编译项目
d:\YH\apache-maven-3.9.6\bin\mvn clean package -DskipTests

# 运行服务
java -jar ..\mahjong-scoreboard-start\target\mahjong-scoreboard-start-1.0-SNAPSHOT.jar
```

## API接口测试顺序

按照以下顺序测试API接口，确保完整的业务流程：

### 1. 创建对局

**接口**：POST /api/v1/matches
**测试脚本**：

```bash
curl -X POST http://localhost:8080/api/v1/matches \
  -H "Content-Type: application/json" \
  -d '{"roomId": 1, "roomName": "快乐棋牌室", "startTime": 1716700800000}'
```

### 2. 添加参与者

**接口**：POST /api/v1/matches/{matchId}/participants
**测试脚本**：

```bash
# 替换{matchId}为实际创建的对局ID
curl -X POST http://localhost:8080/api/v1/matches/1/participants \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "nickname": "玩家1", "avatar": "https://example.com/avatar1.jpg"}'

# 添加更多参与者
curl -X POST http://localhost:8080/api/v1/matches/1/participants \
  -H "Content-Type: application/json" \
  -d '{"userId": null, "nickname": "玩家2", "avatar": "https://example.com/avatar2.jpg"}'
```

### 3. 记录轮次分数

**接口**：POST /api/v1/matches/{matchId}/rounds/{roundNumber}
**测试脚本**：

```bash
# 记录第1轮分数
curl -X POST http://localhost:8080/api/v1/matches/1/rounds/1 \
  -H "Content-Type: application/json" \
  -d '[{"participantId": 1, "score": 50}, {"participantId": 2, "score": -50}]'

# 记录第2轮分数
curl -X POST http://localhost:8080/api/v1/matches/1/rounds/2 \
  -H "Content-Type: application/json" \
  -d '[{"participantId": 1, "score": 30}, {"participantId": 2, "score": -30}]'
```

### 4. 获取参与者列表

**接口**：GET /api/v1/matches/{matchId}/participants
**测试脚本**：

```bash
curl -X GET http://localhost:8080/api/v1/matches/1/participants
```

### 5. 执行对局结算

**接口**：POST /api/v1/matches/{matchId}/settle
**测试脚本**：

```bash
curl -X POST http://localhost:8080/api/v1/matches/1/settle \
  -H "Content-Type: application/json" \
  -d '{"multiplier": 2.0, "notes": "周末双倍积分"}'
```

### 6. 获取结算信息

**接口**：GET /api/v1/matches/{matchId}/settlement
**测试脚本**：

```bash
curl -X GET http://localhost:8080/api/v1/matches/1/settlement
```

## 完整测试脚本（JavaScript）

创建一个`test_api.js`文件，使用Node.js测试所有接口：

```javascript
const axios = require('axios');

// API基础URL
const BASE_URL = 'http://localhost:8080/api/v1';

async function testMatchAPI() {
  console.log('开始测试麻将对局记录API...');
  
  try {
    // 1. 创建对局
    console.log('\n1. 创建对局...');
    const createMatchResponse = await axios.post(`${BASE_URL}/matches`, {
      roomId: 1,
      roomName: '快乐棋牌室',
      startTime: Date.now()
    });
    console.log('创建对局成功:', createMatchResponse.data);
    const matchId = createMatchResponse.data.data.matchId;
    
    // 2. 添加参与者
    console.log('\n2. 添加参与者...');
    const participant1Response = await axios.post(`${BASE_URL}/matches/${matchId}/participants`, {
      userId: 1,
      nickname: '玩家1',
      avatar: 'https://example.com/avatar1.jpg'
    });
    console.log('添加参与者1成功:', participant1Response.data);
    const participantId1 = participant1Response.data.data.participantId;
    
    const participant2Response = await axios.post(`${BASE_URL}/matches/${matchId}/participants`, {
      userId: null,
      nickname: '玩家2',
      avatar: 'https://example.com/avatar2.jpg'
    });
    console.log('添加参与者2成功:', participant2Response.data);
    const participantId2 = participant2Response.data.data.participantId;
    
    // 3. 记录轮次分数
    console.log('\n3. 记录轮次分数...');
    const round1Response = await axios.post(`${BASE_URL}/matches/${matchId}/rounds/1`, [
      { participantId: participantId1, score: 50 },
      { participantId: participantId2, score: -50 }
    ]);
    console.log('记录第1轮分数成功:', round1Response.data);
    
    const round2Response = await axios.post(`${BASE_URL}/matches/${matchId}/rounds/2`, [
      { participantId: participantId1, score: 30 },
      { participantId: participantId2, score: -30 }
    ]);
    console.log('记录第2轮分数成功:', round2Response.data);
    
    // 4. 获取参与者列表
    console.log('\n4. 获取参与者列表...');
    const participantsResponse = await axios.get(`${BASE_URL}/matches/${matchId}/participants`);
    console.log('获取参与者列表成功:', participantsResponse.data);
    
    // 5. 执行对局结算
    console.log('\n5. 执行对局结算...');
    const settleResponse = await axios.post(`${BASE_URL}/matches/${matchId}/settle`, {
      multiplier: 2.0,
      notes: '周末双倍积分'
    });
    console.log('执行对局结算成功:', settleResponse.data);
    
    // 6. 获取结算信息
    console.log('\n6. 获取结算信息...');
    const settlementResponse = await axios.get(`${BASE_URL}/matches/${matchId}/settlement`);
    console.log('获取结算信息成功:', settlementResponse.data);
    
    console.log('\n🎉 所有API测试完成！');
    
  } catch (error) {
    console.error('测试过程中发生错误:', error.message);
    if (error.response) {
      console.error('错误响应:', error.response.data);
    }
  }
}

// 运行测试
testMatchAPI();
```

## 测试注意事项

1. **数据库连接**：确保PostgreSQL数据库服务在localhost:5432端口运行，数据库名为`mahjong_db`，用户名`postgres`，密码`cch815566`

2. **服务启动顺序**：
   - 先启动数据库服务
   - 再启动后端应用服务
   - 确认服务运行在8080端口

3. **依赖检查**：
   - 确保Java环境正常（JDK 17或更高版本）
   - 确保Maven环境正常
   - 对于JavaScript测试脚本，需要安装Node.js和axios

4. **常见问题排查**：
   - 403错误：检查权限配置
   - 500错误：检查数据库连接和服务日志
   - 404错误：确认接口路径正确

5. **服务验证**：
   - 服务启动后，可以先访问 http://localhost:8080/api/test/hello 验证服务是否正常运行
   - 检查日志输出，确认数据库连接成功

## 自动化测试工具

除了手动测试，您还可以使用以下工具进行API测试：

1. **Postman**：创建API测试集合，导入接口文档
2. **Swagger UI**：如果服务集成了Swagger，可以通过http://localhost:8080/swagger-ui.html访问
3. **JMeter**：进行性能测试和负载测试

祝您测试顺利！