import psycopg2
from random import choice

conn = psycopg2.connect(dbname="postgres",
                        user="",
                        password="",
                        host="periphery.ctqxvyj4v2ke.us-east-1.rds.amazonaws.com")

conn.autocommit = True
cur = conn.cursor()
print("Connected!")

cur.execute('drop table if exists have;')
cur.execute('create table have(cid text, book_id text, numcopies bigint);')

cur.execute("select book_id from books;")
books = [book_id[0] for book_id in cur]

cur.execute("select cid from charters;")
charters = [cid[0] for cid in cur]

count = 0
for book in books:
    rand = choice(charters)
    # print(rand, book)
    query = f"insert into have values ('{rand}', '{book}', 1);"
    cur.execute(query)
    count += 1
    if count%1000 == 0:
        print(count)

conn.close()