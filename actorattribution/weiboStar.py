# coding=utf-8
"""
created on:2017.8.17
author:DilicelSten
target:获取电影主演的微博信息
requirement:一部电影一个txt,内容为每个明星一行:名字,粉丝数,关注数,微博数,主页转发总数,评论,点赞,发表时间跨度
finished on:2017.8.17
"""
from __future__ import division
from selenium import webdriver
import re
import datetime
import os
import random
import time
import sys
reload(sys)
sys.setdefaultencoding('utf8')
browser = webdriver.Firefox()  # 启动浏览器


def add_each(number_list):
    """
    对列表中的数字进行加
    :param number_list:
    :return:总数
    """
    result = 0
    for each in number_list:
        result += int(each)
    # print result,number_list
    if len(number_list) == 0:
        result = 0
    else:
        result = int(result/len(number_list))
    return result


def time_process(time_list):
    result = 0
    time_line = []
    day = {}
    for each in time_list:
        tokens = each.split(' ')
        time_line.append(tokens[0])
    i = 0
    for items in time_line:
        # print items
        if u'分钟' in items:
            day[i] = datetime.datetime(2017,8,17)
        elif u'今天' in items:
            day[i] = datetime.datetime(2017,8,17)
        elif u'月' in items:
            tokens1 = items.split('月')
            # print tokens1[0],tokens1[1]
            # print int(tokens1[0]), int(tokens1[1].replace('日', ''))
            day[i] = datetime.datetime(2017, int(tokens1[0]), int(tokens1[1].replace('日', '')))
        else:
            tokens2 = items.split('-')
            # print int(tokens2[0]),int(tokens2[1]),int(tokens2[2])
            day[i] = datetime.datetime(int(tokens2[0]), int(tokens2[1]), int(tokens2[2]))
        i += 1
    for j in range(len(day)):
        # print result
        if j == (len(day)-1):
            break
        result += (day[j] - day[j+1]).days
    if len(day) == 0:
        result = 0
    else:
        result = int(result/len(day))
    return result


def login():
    """
    进行微博的手动登陆
    :return: null
    """
    browser.get("http://weibo.com/")
    while True:
        flag = raw_input('继续请输入Y，否则按任意键')
        if flag.upper() == 'Y':
            break


def movie_id():
    """
    读取电影的id——>电影名
    :return:电影id对应字典
    """
    name_id = {}
    for i in range(2010,2018):
        path = '../data/MovieData/'+str(i)+'.json'
        with open(path, 'r') as f:
            total = f.read()
        pattern_id = re.compile("\"id\":(.*?)\n")
        id_list = re.findall(pattern_id, total)
        pattern_name = re.compile("\"MovieName\":(.*?)\n")
        name_list = re.findall(pattern_name, total)
        for j in range(len(id_list)):
            name_id[id_list[j]] = name_list[j]
    return name_id


def movie_actor():
    """
    获取电影中的主演名字
    :return: 电影——>主演列表的字典
    """
    movieName = movie_id()
    movie_star = {}
    for i in range(2010, 2018):
        path = '../data/MovieActorData/'+str(i)+'/'
        file_list = os.listdir(path)
        for each in file_list:
            actor_list = []
            file_path = path + each
            with open(file_path, 'r') as f:
                lines = f.readlines()
                for line in lines:
                    tokens = line.split('	')
                    actor_list.append(tokens[0])
            movie_star[movieName[each]] = actor_list
    return movie_star


def actor_info(actor_name):
    """
    获取演员的微博的信息
    :param actor_name:演员名
    :return: 演员的一串信息
    """
    url = 'https://weibo.cn/n/' + actor_name
    browser.get(url)
    html = browser.page_source
    with open('../data/starHtml/'+actor_name+'.txt','w') as fw:
        fw.write(html)
    if u'此用户不存在或更改了名字' in html:
        print actor_name + '用户不存在'
        info = actor_name+',null'
    elif u'<span class="cmt">共' in html:
        print actor_name + '用户名字没找对'
        info = actor_name + ',null'
    elif u'抱歉，未找到' in html:
        print actor_name + '用户名字没找到'
        info = actor_name + ',null'
    elif u'转发' and u'评论' and u'赞' in html:
        browser.get(url)
        html = browser.page_source
        fans_pattern = re.compile('fans">.*?\[(.*?)\]</a>')
        fans_result = re.findall(fans_pattern, html)
        follow_pattern = re.compile('follow">.*?\[(.*?)\]</a>')
        follow_result = re.findall(follow_pattern, html)
        weibos_pattern = re.compile('"tc">.*?\[(.*?)\]</span>')
        weibos_result = re.findall(weibos_pattern, html)
        repost_pattern = re.compile('0">'+u'转发'+'\[(.*?)]</a>')
        repost_result = add_each(re.findall(repost_pattern, html))
        comment_pattern = re.compile('"cc">'+u'评论'+'\[(.*?)]</a>')
        comment_result = add_each(re.findall(comment_pattern, html))
        good_pattern = re.compile('cmt">'+u'赞'+'\[(.*?)]</span>')
        good_result = add_each(re.findall(good_pattern, html))
        time_pattern = re.compile('class="ct">(.*?)'+u'来'+'.*?</span>')
        time_result = time_process(re.findall(time_pattern, html))
        info = actor_name+','+fans_result[0]+','+follow_result[0]+','+weibos_result[0]+','+str(repost_result)+','+str(comment_result)+','+str(good_result)+','+str(time_result)
    else:
        print actor_name + '被微博误伤了'
        info = actor_name + ',null'
    return info


if __name__ == '__main__':
    login()
    movie_star = movie_actor()
    moviename_id = movie_id()
    for i in range(2010, 2018):
        print i
        path = '../data/MovieActorData/' + str(i) + '/'
        file_list = os.listdir(path)
        for each in file_list:
            star_list = movie_star[moviename_id[each]]
            print moviename_id[each]
            if os.path.exists('../data/weiboActor/' + str(i) + '/' + moviename_id[each] + '.txt'):
                continue
            else:
                with open('../data/weiboActor/' + str(i) + '/' + moviename_id[each] + '.txt', 'w') as f:
                    for they in star_list:
                        n = random.randint(1, 6)
                        time.sleep(n)
                        f.write(actor_info(they)+'\n')
    # print actor_info('刘烨')