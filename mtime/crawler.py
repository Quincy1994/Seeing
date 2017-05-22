# coding: utf-8

import sys
import os

reload(sys)
sys.setdefaultencoding('utf-8')

#coding=utf-8
import re
import urllib
import urllib2
import time

user_agent = 'Mozilla/4.0 (compatible; MSIE 5.5; Windows NT)'
headers = {'User-Agent': user_agent}
number=0


def crawler_url(comment_url):
    content = urllib2.urlopen(comment_url).read().decode('utf-8').replace("\n", "")
    # print content
    pattern = re.compile('<a target="_blank" title=".*?" href="http://my.mtime.com/(.*?)/"><img width.*?></a>')
    items = re.findall(pattern, content)
    return items


def crawler_words(user_id):
    wordXml = '<words>'
    pageNum = 11
    for i in range(1, pageNum, 1):
        word_url = 'http://sandbox.my.mtime.com/Service/callback.mc?Ajax_CallBack=true&Ajax_CallBackType=Mtime.MemberCenter.Pages.CallbackService&Ajax_CallBackMethod=RemoteLoad&Ajax_CrossDomain=1&Ajax_RequestUrl=http%3A%2F%2Fmy.mtime.com%2F' + user_id + '%2F&Ajax_CallBackArgument0=t&Ajax_CallBackArgument1=' + user_id + '%2F%3Ffilter%3D0%26pageIndex%3D' + str(i)
        try:
            content = urllib2.urlopen(word_url, timeout=5).read().decode('utf-8').replace("\n", "")
            time.sleep(2)
        except:
            content = urllib2.urlopen(word_url).read().decode('utf-8').replace("\n", "")
            time.sleep(3)
        pattern_words = re.compile(u'<strong class=."c_green ml6 mr6 px16.">说</strong>(.*?)<')
        words = re.findall(pattern_words, content)
        for word in words:
            wordXml += word + ','
    wordXml += '</words>'
    print wordXml
    return wordXml

def crawler_area(user_id):
    xml = ''

    # 爬取用户的个人主页
    user_url = 'http://sandbox.my.mtime.com/Service/callback.mc?Ajax_CallBack=true&Ajax_CallBackType=Mtime.MemberCenter.Pages.CallbackService&Ajax_CallBackMethod=RemoteLoad&Ajax_CrossDomain=1&Ajax_RequestUrl=http%3A%2F%2Fmy.mtime.com%2F' + user_id + '%2F&Ajax_CallBackArgument0=t&Ajax_CallBackArgument1=' + user_id + '%2F%3F%242'
    try:
        content = urllib2.urlopen(user_url, timeout=3).read().decode('utf-8').replace("\n", "")
        time.sleep(3)
    except:
        content = urllib2.urlopen(user_url).read().decode('utf-8').replace("\n", "")
        time.sleep(3)

    # 获取性别
    sex_xml = '<sex></sex>'
    pattern_sex = re.compile(u'<p class=."mt9.">(.*?)</p>.*?<p class=."mt6.">个人博客')
    items = re.findall(pattern_sex, content)
    if items:
        sex = items[0]
        if '女' in sex:
            sex_xml = '<sex>女</sex>'
        if '男' in sex:
            sex_xml = '<sex>男</sex>'

    # 获取博客网址加地区
    pattern_blog = r'<dt class=."normal."><a href=."http://my.mtime.com/(.*?)/." method'
    matcher_blog = re.findall(pattern_blog, content)
    if not matcher_blog:
        return ''
    blog_url = matcher_blog[0]
    blog_url = 'http://i.mtime.com/' + blog_url
    place = get_place(blog_url)
    if place is None:
        return xml
    place = place.replace(" ","")
    place_xml = '<area>' + place + '</area>'

    # 获取年龄
    age_xml = get_age(blog_url)
    if age_xml == '<age></age>':
        return xml

    # 获取教育背景
    education_xml = '<education>'
    pattern_education = u'div class=."pt20 tr_link."> <h4 class=."px14.">教育&职业</h4>(.*?)</div>'
    block_education = re.findall(pattern_education, content)
    education = ''
    if block_education:
        education = block_education[0]
    pattern_education_one = u'<a href.*title=.".*?">(.*?)</a>'
    education_one = re.findall(pattern_education_one, education)
    for one in education_one:
        education_xml += one + ','
    education_xml += '</education>'

    # 获取群组
    group_xml = '<group>'
    pattern_group = re.compile(
        u'class=."img_box." alt=."群组昵称." /></a> <p><a href=."http://group.mtime.com/.*?/." target=."_blank." title=.".*?">(.*?)</a>')
    groups = re.findall(pattern_group, content)
    groupTemp = ''
    for group in groups:
        groupTemp += group + ','
    group_xml += groupTemp + '</group>'

    # 获取用户的id
    pattern_id = 'userid=."(.*?)"'
    id_list = re.findall(pattern_id, content)
    id = id_list[0]
    id = id.replace("\\","")

    # 获取收藏
    colletion_xml = get_collection(blog_url, id)

    # 获取用语
    words_xml = crawler_words(user_id)

    # print place_xml
    # print group_xml
    # print education_xml
    # print colletion_xml
    # print words_xml

    xml += age_xml + sex_xml +place_xml + group_xml + education_xml + colletion_xml + words_xml +'\n'
    print xml
    return xml

def get_age(blog_url):
    age_xml = '<age>'
    age = ''
    try:
        content = urllib2.urlopen(blog_url, timeout=3).read().decode('utf-8').replace("\n", "")
        time.sleep(1)
    except:
        content = urllib2.urlopen(blog_url).read().decode('utf-8').replace("\n", "")
    pattern_age = u'<span id="ownerAgeRegion">(.*?)岁&nbsp;'
    matcher_age = re.findall(pattern_age, content)
    # print re.match(pattern_place, content)
    if matcher_age:
        age = matcher_age.pop()
    age_xml += age + '</age>'
    return age_xml

def get_place(blog_url):
    try:
        content = urllib2.urlopen(blog_url, timeout=3).read().decode('utf-8').replace("\n", "")
        time.sleep(1)
    except:
        content = urllib2.urlopen(blog_url).read().decode('utf-8').replace("\n", "")
    pattern_place = u'居住在：(.*?)</p>'
    matcher_place = re.findall(pattern_place, content)
    # print re.match(pattern_place, content)
    if matcher_place:
        place = matcher_place.pop()
        return place
    return None



def get_collection(blog_url, id):
    collection_xml = '<collection>'
    url = 'http://i.mtime.com/service/user.mb?Ajax_CallBack=true&Ajax_CallBackType=Mtime.Blog.Pages.UserService&Ajax_CallBackMethod=GetHomeModuleCallbacks&Ajax_RequestUrl='+ blog_url + '/&Ajax_CallBackArgument0=Mtime.Blog.Pages.GroupService|GetHomeGroups|' + id + '_1_6-Mtime.Blog.Pages.QuizService|GetHomeQuizs|'+ id + '_5-Mtime.Blog.Pages.AlbumService|GetHomeAlbums|'+ id + '_4_true_4-Mtime.Blog.Pages.UserService|GetLeavingMessages|' + id + '_20-Mtime.Blog.Pages.FavoriteService|GetHomeFavorites|'+ id + '_3_6_true-Mtime.Blog.Pages.UserService|GetHomeLinks|' + id + '_13_13'
    # print url
    try:
        content = urllib2.urlopen(url, timeout=3).read().decode('utf-8').replace("\n", "")
        time.sleep(1)
    except:
        content = urllib2.urlopen(url).read().decode('utf-8').replace("\n", "")
        time.sleep(2)
    pattern_collection = r'title=.*?"(.*?)"'
    # print content
    collections = re.findall(pattern_collection, content)
    for one in collections:
        one = one.replace("\\","")
        collection_xml += one + ','
    collection_xml += '</collection>'
    # print collection_xml
    return collection_xml

def crawler_tags(user_id):
    xml = ''
    sex = None
    groupxml = '<group>'
    tags = '<tag>'
    user_url = 'http://sandbox.my.mtime.com/Service/callback.mc?Ajax_CallBack=true&Ajax_CallBackType=Mtime.MemberCenter.Pages.CallbackService&Ajax_CallBackMethod=RemoteLoad&Ajax_CrossDomain=1&Ajax_RequestUrl=http%3A%2F%2Fmy.mtime.com%2F'+ user_id +'%2F&Ajax_CallBackArgument0=t&Ajax_CallBackArgument1=' + user_id +'%2F%3F%242'
    try:
        content = urllib2.urlopen(user_url, timeout= 3)
        time.sleep(1)
    except:
        content = urllib2.urlopen(user_url)
        time.sleep(2)
    try:
        content = content.read().decode('utf-8').replace("\n", "")

        # 获取标签
        pattern_tag = re.compile('<a href=."http://my.mtime.com/search/user/.content=(.+?)&searchtype=2." class=."mr6.">')
        tagGroup = re.findall(pattern_tag,content)
        tagTemp = ''
        for tag in tagGroup:
            if tag.__len__() > 20:
                continue
            tagTemp += tag + ","
        if tagTemp == '':
            return tagTemp
        tags += tagTemp + '</tag>'

        # 获取性别
        pattern_sex = re.compile(u'<p class=."mt9.">(.*?)</p>.*?<p class=."mt6.">个人博客')
        items = re.findall(pattern_sex, content)
        sex = items[0]

        # 获取群组
        pattern_group = re.compile(u'class=."img_box." alt=."群组昵称." /></a> <p><a href=."http://group.mtime.com/.*?/." target=."_blank." title=.".*?">(.*?)</a>')
        groups = re.findall(pattern_group, content)
        groupTemp = ''
        for group in groups:
            groupTemp += group + ','
        groupxml += groupTemp + '</group>'
        print groupxml

        # 获取评论
        wordsTemp = crawler_words(user_id)
        # pattern_words = re.compile(u'<strong class=."c_green ml6 mr6 px16.">说</strong>(.*?)<')
        # words = re.findall(pattern_words, content)
        # wordsTemp = '<words>'
        # for word in words:
        #     wordsTemp += word + ','
        # wordsTemp += '</words>'
        print wordsTemp

        if '女' in sex:
            sexXml = '<sex>女</sex>'
        elif '男' in sex:
            sexXml = '<sex>男</sex>'
        else:
            return xml
        xml = sexXml + tags + groupxml + wordsTemp +'\n'
        print xml
        return xml
    except:
        return xml


def get_tag(comment_url):
    user_ids = crawler_url(comment_url)
    xml = ''
    for user_id in user_ids:
        xml += crawler_tags(user_id)
    return xml

def get_area(comment_url):
    user_ids = crawler_url(comment_url)
    xml = ''
    for user_id in user_ids:
        xml += crawler_area(user_id)
    return xml

def writeTag(id):
    global number
    xml = ''
    for i in range(2,11):
        comment_url = 'http://movie.mtime.com/' + id + '/reviews/short/new-' + str(i) + '.html'
        xml += get_tag(comment_url)
    filename = '/home/hadoop/tag0'+ str(number) +'.txt'
    f = open(filename,'w')
    f.write(xml)
    f.close()
    number += 1

def write_area(id):
    global number
    xml = ''
    for i in range(2,11):
        comment_url = 'http://movie.mtime.com/' + id + '/reviews/short/new-' + str(i) + '.html'
        xml += get_area(comment_url)
    filename = '/home/hadoop/movie0'+ str(number) +'.txt'
    f = open(filename,'w')
    f.write(xml)
    f.close()
    number += 1


def get_id(movie_name):
    mtime_url = "http://service.channel.mtime.com/Search.api?Ajax_CallBack=true&Ajax_CallBackType=Mtime.Channel.Services&Ajax_CallBackMethod=GetSearchResult&Ajax_CrossDomain=1&Ajax_RequestUrl=http://search.mtime.com/search/?q="+movie_name+"&t=1&t=2016113021433167701&Ajax_CallBackArgument0="+movie_name+"&Ajax_CallBackArgument1=1&Ajax_CallBackArgument2=290&Ajax_CallBackArgument3=0&Ajax_CallBackArgument4=1"
    content = urllib2.urlopen(mtime_url).read().decode('utf-8').replace("\n", "")
    pattern = re.compile('"movieId":(.*?),"movieTitle"')
    movie_id = re.findall(pattern, content)
    return movie_id


def main():
    movie_name = '生化危机'
    movie_id = get_id(movie_name)
    for id in movie_id:
        write_area(id)

main()