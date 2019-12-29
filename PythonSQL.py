import psycopg2
import json
import html

conn = psycopg2.connect(dbname="postgres",
                        user="",
                        password="",
                        host="periphery.ctqxvyj4v2ke.us-east-1.rds.amazonaws.com")
conn.autocommit = True
cur = conn.cursor()

cur.execute('drop table if exists Charters;')
cur.execute('create table Charters(libname text, image text, stewname text, email text, num text, story text, emailonmap bool, nameonmap bool, lat real, lon real, street text, city text, state text, country text, zipcode text, cid text);')

with open("369_miles.json", encoding="utf8") as read_file:
    charters = json.load(read_file)
    valid = 0
    skipped = 0
    for charter in charters:
        try:
            library = charter["library"]
            libname = html.unescape(library['Library_Name__c']).replace("'","\\'")
            image = charter["attachment1"]
            stewname = html.unescape(library['Primary_Steward_s_Name__c']).replace("'","\\'")
            email = html.unescape(library['Primary_Steward_s_Email__c'])
            num = library['Official_Charter_Number__c']
            story = html.unescape(library['Library_Story__c']).replace("'","\\'")
            emailonmap = library['Email_on_Map__c']
            nameonmap = library['Name_on_Map__c']
            loc = library['Library_Geolocation__c']
            lat = loc['latitude']
            lon = loc['longitude']
            street = html.unescape(library['Street__c']).replace("'","\\'")
            city = html.unescape(library['City__c']).replace("'","\\'")
            state = html.unescape(library['State_Province_Region__c']).replace("'","\\'")
            country = html.unescape(library['Country__c']).replace("'","\\'")
            zipcode = library['Postal_Zip_Code__c']
            cid = library['Id']
            
            valid += 1
        except:
            skipped += 1
            continue
        query = f"insert into Charters values (e'{libname}', '{image}', e'{stewname}', '{email}', '{num}', e'{story}', {emailonmap}, {nameonmap}, {lat}, {lon}, e'{street}', e'{city}', e'{state}', e'{country}', '{zipcode}', '{cid}');"
        cur.execute(query)
    print(f'valid = {valid}, skipped = {skipped}')

# conn.commit()
conn.close()