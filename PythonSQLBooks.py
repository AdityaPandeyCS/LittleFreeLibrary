import psycopg2
import json
import csv
import html

# conn = psycopg2.connect('postgres://periphery:PeripheryLFL@littlefreelibrary.cemkwyu2lwna.us-east-1.rds.amazonaws.com:5432', sslmode='require')
conn = psycopg2.connect(dbname="postgres",
                        user="",
                        password="",
                        host="periphery.ctqxvyj4v2ke.us-east-1.rds.amazonaws.com")

conn.autocommit = True
cur = conn.cursor()
print("Connected!")
cur.execute('drop table if exists Books;')
cur.execute('create table Books(book_id text, image text, title text, author text, genre text, description text);')

ignore_genres = ["Gay & Lesbian","Calendars"]

with open("book32-listing.csv", encoding='ISO-8859-1') as read_file:
    books = csv.reader(read_file, delimiter=",")
    skipped = 0
    valid = 0
    for book in books:
        try:
            genre = html.unescape(book[6]).replace("'","\\'")
            if (genre in ignore_genres):
                raise TypeError
            book_id = html.unescape(book[0]).replace("'","\\'")
            image = html.unescape(book[2]).replace("'","\\'")
            title = html.unescape(book[3]).replace("'","\\'")
            author = html.unescape(book[4]).replace("'","\\'")
            query = f"insert into Books values (e'{book_id}', e'{image}', e'{title}', e'{author}', e'{genre}');"
            valid += 1
        except:
            skipped += 1
            continue

        cur.execute(query)
    print(f'valid = {valid}, skipped = {skipped}')

# conn.commit()
conn.close()