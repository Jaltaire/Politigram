import os

import requests
from bs4 import BeautifulSoup


def data_scraper():

    rep_entries = []

    us_representatives_url = "https://en.wikipedia.org/wiki/Seniority_in_the_United_States_House_of_Representatives"

    session = requests.session()
    html = session.get(us_representatives_url)

    main_html = BeautifulSoup(html.text, "html.parser")
    reps_table = main_html.find("table", {"class": "wikitable sortable"})

    rep_lines = reps_table.findAll("tr")

    for i in range(1, len(rep_lines)):
        data = rep_lines[i].findAll("td")

        try:
            name = data[0].find("span").find("span").find("span").find("a").get('title')
            url = data[0].find("span").find("span").find("span").find("a").get('href')
        except AttributeError:
            name = data[0].find("a").get('title')
            url = data[0].find("a").get('href')

        party = data[1].text.strip()

        rep = Representative(name, party, url)
        rep_entries.append(rep)

    return rep_entries


def image_scraper(rep_entries):

    for i in range(0, len(rep_entries)):
        try:
            get_image(rep_entries[i], i)
        except ConnectionResetError:
            get_image(rep_entries[i], i)


def get_image(rep, i):

    base_wikipedia_url = "https://en.wikipedia.org"

    name = rep.name
    party = rep.party
    wiki_url = rep.url

    session = requests.session()
    html = session.get(base_wikipedia_url + wiki_url)
    main_html = BeautifulSoup(html.text, "html.parser")

    image_data = str(main_html.find("table", {"class": "infobox vcard"}).find("img").get('srcset'))
    image_options = image_data.split(",")

    try:
        higher_res = image_options[1].strip().strip("//")
    except IndexError:
        higher_res = image_options[0].strip().strip("//")

    higher_res_no_metadata = higher_res.split(" ")[0].strip()

    if party == "D":
        political_leaning = "Liberal"
    elif party == "R":
        political_leaning = "Conservative"
    else:
        political_leaning = "Independent"

    #name_formatted = ''.join([name_comp for name_comp in name.split(" ")])
    name_formatted = str(i)

    image_path = political_leaning + "/" + name_formatted + ".jpg"
    # image_path = "/" + political_leaning

    full_dir_name = os.path.dirname(__file__)
    dirname = os.path.dirname(full_dir_name + "/" + image_path)
    if not os.path.exists(dirname):
        os.makedirs(dirname)

    image_request = requests.get("http://" + higher_res_no_metadata)
    if image_request.status_code == 200:
        with open(image_path, 'wb') as f:
            f.write(image_request.content)


class Representative:

    def __init__(self, name, party, url):
        self.name = name
        self.party = party
        self.url = url

    def __repr__(self):
        return str([self.name, self.party, self.url])


rep_entries = data_scraper()
image_scraper(rep_entries)
