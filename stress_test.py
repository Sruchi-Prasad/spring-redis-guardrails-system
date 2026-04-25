import requests
import threading
import time

# Configuration
BASE_URL = "http://localhost:8080/api/posts"
POST_ID = 1
NUM_REQUESTS = 200
USER_ID_START = 1000

def send_bot_comment(user_id):
    url = f"{BASE_URL}/{POST_ID}/comments"
    payload = {
        "authorId": user_id,
        "content": f"Bot comment from user {user_id}",
        "depthLevel": 1
    }
    # Parameter isBot triggers specific guardrails
    params = {"isBot": "true"}
    
    try:
        response = requests.post(url, json=payload, params=params)
        print(f"User {user_id}: Status {response.status_code} - {response.text}")
    except Exception as e:
        print(f"User {user_id}: Error {str(e)}")

def run_stress_test():
    print(f"Starting stress test: {NUM_REQUESTS} concurrent bot comments on Post {POST_ID}...")
    threads = []
    
    start_time = time.time()
    
    for i in range(NUM_REQUESTS):
        user_id = USER_ID_START + i
        t = threading.Thread(target=send_bot_comment, args=(user_id,))
        threads.append(t)
        t.start()
    
    for t in threads:
        t.join()
    
    end_time = time.time()
    print(f"\nStress test complete in {end_time - start_time:.2f} seconds.")
    print("Check server logs for bot virality capping and cooldown enforcement.")

if __name__ == "__main__":
    run_stress_test()
