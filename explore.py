from twilio.rest import Client

client = Client(
    "AC94d8271752396603651e0e4d7b91b8cb",
    "a334a4d128903b6986a96ecaeacd8b9e"
)


for msg in client.messages.list():
    print(f"Deleting {msg.body}")
    msg.delete()




""" msg  = client.messages.create(
    to = "+447887379330",
    from_ = "+12565769374",
    body = "Hello from Python",
)

print(f"Created a new message: {msg.sid}") """