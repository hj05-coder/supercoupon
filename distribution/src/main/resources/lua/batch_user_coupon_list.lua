-- 获取传递的参数
local userIds = cjson.decode(ARGV[1])  -- 用户 ID 集合，JSON 格式的字符串
local couponIds = cjson.decode(ARGV[2])  -- 优惠券 ID 集合，JSON 格式的字符串
local userIdPrefix = KEYS[1]  -- 用户 ID 前缀（从 KEYS 获取）
local limitKeyPrefix = KEYS[2]  -- 用户优惠券模板限制前缀
local couponTemplateId = KEYS[3]  -- 优惠券模板 ID
local currentTime = tonumber(ARGV[3])  -- 获取当前 Unix 时间戳（毫秒）
local couponTemplateValidEndTime = tonumber(ARGV[4])  -- 优惠券模板到期时间

-- 遍历用户 ID 集合
for i, userId in ipairs(userIds) do
    local key = userIdPrefix .. userId  -- 拼接用户 ID 前缀和用户 ID
    local couponId = couponIds[i]  -- 获取对应的优惠券 ID
    if couponId then
        redis.call('ZADD', key, currentTime, couponId)  -- 添加优惠券 ID 到 ZSet 中

        local limitKey = limitKeyPrefix .. userId .. '_' ..  couponTemplateId
        redis.call('INCR', limitKey)  -- 添加用户和优惠券的领取次数，方便后续对用户进行领取次数前置限制
        redis.call('EXPIRE', limitKey, couponTemplateValidEndTime)  -- 添加用户优惠券模板限制领取 Key 过期时间
    end
end
