# Social Media Interaction Layer: Redis-First Guardrails System

A high-performance Spring Boot microservice designed for high-concurrency interaction workloads.

## 🧱 Architecture
- **Redis-First**: Real-time decisions and atomic counting using Distributed Redis.
- **Strong Consistency**: Guaranteed for user actions (likes/comments) via Redis atomic operations (INCR/HINCRBY).
- **Eventual Consistency**: Implemented for the analytics aggregation pipeline.
- **Degraded Mode**: Failure-resilient design scales back to "fail-open" mode to maintain availability if Redis is unreachable.

## 🛡️ Guardrails
1. **Idempotency**: 15-minute action lock window per `User:Post:Action` tuple to prevent double-counting.
2. **Vertical Cap**: Strict enforcement of comment depth limits (Max Depth: 20).
3. **Horizontal Cap**: Bot interaction virality "freeze" after 100 per post lifecycle. Analytics continue to log, but virality contribution stops.
4. **Rate Limiting**: 10-minute enforcement window for bot-based actors.

## 📊 Analytics & Notifications
- **Async Pipeline**: Background cron job aggregates metrics into `PostAnalytics` table every 5 minutes.
- **Smart Notifications**: Batched notification system that summarizes multiple interactions into single push alerts.

## 🧠 Redis Keys & Distributed State
- `post:{id}:virality_score`: Weighted sum of interactions (Likes: +20, Comments: +50).
- `post:{id}:bot_count`: Atomic counter for bot interactions with enforcement capping.
- `post:{id}:likes_count`: Precise counter for human + bot likes for durable analytics.
- `user:bot_cooldown:{id}`: TTL-based lock for bot rate-limiting (10 min).
- `user:action:{uid}:{pid}:{type}`: Atomically managed idempotency window (15 min).
- `user:{id}:notif_cooldown`: TTL gate for smart notification batching.
- `user:{id}:pending_notifs`: Redis List used for asynchronous notification queuing.

## 🧪 Stress Testing
The system includes a Python-based stress test script to validate high-concurrency performance and guardrail enforcement.
- **Requirement**: `pip install requests`
- **Run**: `python stress_test.py`
- **Output**: Simulates 200 concurrent bot interactions, triggers virality capping, and ensures 100% data integrity under load.

## 🏗️ System Highlights
- **Hybrid Storage**: PostgreSQL serves as the persistent system of record for all durable entities (Posts, Comments, Analytics and Likes), while Redis acts as the real-time gatekeeper for high-concurrency guardrails.
- **Concurrency**: Tested under 200+ concurrent requests per post without race conditions, leveraging Redis atomic primitives.
- **Degraded Mode (Resilience)**: During Redis outages, the system utilizes a "Fail-Open" design. Requests **bypass guardrails instead of failing**, ensuring core business operations remain available while exceptions are gracefully trapped and logged.

## 🚀 API Contract
- `POST /api/posts` -> Creates a new post. (Body: `authorId`, `content`)
- `POST /api/posts/{id}/like?userId={userId}` -> Atomic like increment.
- `POST /api/posts/{id}/comments` -> Threaded comment with guardrails. (Body: `authorId`, `content`, `depthLevel`)

> [!TIP]
> When testing with Postman, replace `{id}` (postId) in the URLs dynamically after creating a post to ensure you are targeting a valid record.

---
*System designed to simulate high-throughput social media interaction layers using Redis-first architecture.*
