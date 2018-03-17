# coding=utf-8
"""
created on:2017/9/6
author:DilecelSten
target:提取mongodb中的数据形成txt文件
finished on:2017/9/7
"""
from pymongo import MongoClient
import re


def connect_mongodb():
    """
    连接mongodb
    :return:数据库
    """
    # 建立mongodb数据库连接
    client = MongoClient('192.168.235.32', 27017)
    # 用户验证
    db = client.movie
    db.authenticate("root", "iiip")
    print ("报告!数据库连接成功!")
    return db


def extract_baidu_index(db, id):
    """
    提取百度指数（所有演员求平均）
    :param db:数据库
    :param id: 电影编号
    :return:
    """
    # 连接所用集合，也就是我们通常所说的表
    collection = db.actor_baiduIndex
    result = 0
    if not collection.find_one({'_id': id}):
        final_result = 0
    else:
        # n = len(collection.find_one({'_id': id})['actor_baidu_index'])
        for key in collection.find_one({'_id': id})['actor_baidu_index']:
            result += collection.find_one({'_id': id})['actor_baidu_index'][key]
        final_result = result
    return final_result


def extract_actor_price(db, id):
    """
    提取每部电影所有演员获奖与提名的平均
    :param db: 数据库
    :param id: 电影id
    :return: 所有演员获奖与提名的平均
    """
    collection = db.actor_price
    result = 0
    if not collection.find_one({'_id': id}):
        final_result = 0
    else:
        n = len(collection.find_one({'_id': id})['actor_list'])
        for each in collection.find_one({'_id': id})['actor_list']:
            result += each['price_num'] + each['nomination_num']
        if n == 0:
            final_result = 0
        else:
            final_result = result
    return final_result


def extract_fans_love(db, id):
    """
    提取演员的影迷喜好度并求平均
    :param db:数据库
    :param id:电影id
    :return:
    """
    collection = db.actor_fans_love
    result = 0
    if not collection.find_one({'_id': id}):
        final_result = 0
    else:
        n = len(collection.find_one({'_id': id})['actor_fans_love'])
        for key in collection.find_one({'_id': id})['actor_fans_love']:
            result += collection.find_one({'_id': id})['actor_fans_love'][key]
        if n == 0:
            final_result = 0
        else:
            final_result = result
    return final_result


def extract_weibo_index(db, id):
    """
    提取电影的微博号召力（每部电影的最高粉丝数）
    :param db:数据库
    :param id:电影id
    :return:最高粉丝数
    """
    collection = db.actor_weibo
    result = 0
    if collection.find_one({'_id': id}):
        # n = len(collection.find_one({'_id': id})['actor_list'])
        for each in collection.find_one({'_id': id})['actor_list']:
            result += each['fans_num'] + each['follow_num'] + each['influence'] + each['weibo_num']
    else:
        result = 0
    final_result = result
    return final_result


def extract_type_match(db, id):
    """
    提取电影演员的作品匹配程度
    :param db: 数据库
    :param id: 电影id
    :return: 匹配程度的平均
    """
    collection = db.actor_match_type
    result = 0
    if not collection.find_one({'_id': id}):
        final_result = 0
    else:
        n = len(collection.find_one({'_id': id})['actor_match_type'])
        for key in collection.find_one({'_id': id})['actor_match_type']:
            result += float(collection.find_one({'_id': id})['actor_match_type'][key])
        if n == 0:
            final_result = 0
        else:
            final_result = result
    return final_result


def extract_actor_boxoffice(db, id):
    """
    提取演员的历史票房求平均
    :param db:
    :param id:
    :return: 历史票房平均值
    """
    collection = db.actor_graph
    result = 0
    if not collection.find_one({'_id': id}):
        final_result = 0
    else:
        n = len(collection.find_one({'_id': id})['actor_list'])
        for key in collection.find_one({'_id': id})['actor_list']:
            result += float(key['avg_boxOffice'])
        if n == 0:
            final_result = 0
        else:
            final_result = result
    return final_result


def extract_douban_score(db, id):
    """
    提取电影演员历史的豆瓣评分
    :param db:
    :param id:
    :return: 豆瓣评分的平均值
    """
    collection = db.actor_doubanScore
    result = 0
    if not collection.find_one({'_id': id}):
        final_result = 0
    else:
        n = len(collection.find_one({'_id': id})['actor_douban_score'])
        for key in collection.find_one({'_id': id})['actor_douban_score']:
            result += float(collection.find_one({'_id': id})['actor_douban_score'][key])
        if n == 0:
            final_result = 0
        else:
            final_result = result
    return final_result


def extract_newpercentages(db, id):
    """
    提取电影的新闻覆盖率
    :param db:
    :param id:
    :return:
    """
    collection = db.actor_newpercentages
    result = 0
    if not collection.find_one({'_id': id}):
        final_result = 0
    else:
        n = len(collection.find_one({'_id': id})['actor_newpercentages']) + 1
        if n == 0:
            final_result = 0
        else:
            for key in collection.find_one({'_id': id})['actor_newpercentages']:
                result += float(collection.find_one({'_id': id})['actor_newpercentages'][key])
                if collection.find_one({'_id': id})['actor_newpercentages'][key] == 0:
                    n -= 1
            final_result = result / n
    return final_result


def extract_actor_comment(db, id):
    """
    提取演员的评论覆盖率
    :param db:
    :param id:
    :return:
    """
    collection = db.actor_emotion_score
    result = 0
    if not collection.find_one({'_id': id}):
        final_result = 0
    else:
        n = len(collection.find_one({'_id': id})['actor_emotion_score'])
        for key in collection.find_one({'_id': id})['actor_emotion_score']:
            result += float(collection.find_one({'_id': id})['actor_emotion_score'][key])
        if n == 0:
            final_result = 0
        else:
            final_result = result
    return final_result

def get_three_score(db, id):
    """
    获取电影的3个评分
    :param db: 数据库
    :param id: 电影id
    :return:3个评分（微博,豆瓣,时光网）
    """
    collection = db.movie_info
    if not collection.find_one({'_id': id}):
        weibo_score = 0
        douban_score = 0
        mtime_score = 0
    else:
        weibo_score = collection.find_one({'_id': id})['movie_info']['weibo_score']
        douban_score = collection.find_one({'_id': id})['movie_info']['douban_score']
        mtime_score = collection.find_one({'_id': id})['movie_info']['mtime_score']
    return weibo_score, douban_score, mtime_score


def get_movie_id():
    """
    获取所有电影的id
    :return: 电影id列表
    """
    id_match = get_total_id()
    year_id = {}
    for i in range(2010, 2018):
        result_list = []
        path = '../data/douban_id_filter/' + str(i) + '.txt'
        with open(path, 'r') as f:
            total = f.read()
        pattern_id = re.compile("(.*?)\n")
        douban_id_list = re.findall(pattern_id, total)
        for each in douban_id_list:
            result_list.append(id_match[each])
        year_id[i] = result_list
    return year_id


def get_box_office(movie_id):
    """
    获取电影的总票房
    :param movie_id:电影id
    :return: 票房
    """
    box_dict = {}
    with open('../data/movie_info.txt') as f:
        lines = f.readlines()
        for each in lines:
            tokens = each.split(':')
            box_dict[tokens[0]] = int(tokens[1].strip('\n'))
    return box_dict[movie_id]


def get_total_id():
    """
    id匹配
    :return:
    """
    id_match = {}
    with open('../data/total.txt', 'r') as f:
        lines = f.readlines()
        for each in lines:
            tokens = each.split('-')
            id_match[tokens[2].strip('\n')] = tokens[1]
    return id_match


def write_thing(db, each):
    """
    写入票房文件
    :param db:
    :param each:
    :return:
    """
    result = str(get_box_office(each))+','+str(extract_actor_price(db,each))+','+str(extract_fans_love(db,each))+','+str(extract_weibo_index(db,each))+','+str(extract_type_match(db,each))+','+str(extract_actor_boxoffice(db,each))+','+str(extract_newpercentages(db,each))+','+str(extract_actor_comment(db,each))+'\n'
    return result


def write_score(db, each, a):
    """
    写入评分文件
    :param db:
    :param each:
    :return:
    """
    result = str(get_three_score(db, int(each))[a]) + ',' + str(
                        extract_actor_price(db, each)) + ',' + str(extract_fans_love(db, each)) + ',' + str(
                        extract_weibo_index(db, each)) + ',' + str(extract_type_match(db, each))+','+str(extract_actor_boxoffice(db,each))+','+str(extract_newpercentages(db,each)) +','+str(extract_actor_comment(db,each)) + '\n'
    return result


if __name__ == '__main__':
    name_list = ['weibo_score', 'douban_score', 'mtime_score']
    db = connect_mongodb()
    id_list = get_movie_id()
    for i in range(2010, 2018):
        with open('../data/new_model_trainging/'+str(i)+'/boxoffice.txt', 'w') as f:
            for each in id_list[i]:
                if extract_actor_comment(db,each) == 0:
                    print 'null'
                    continue
                else:
                    print each
                    f.write(write_thing(db, each))
        for a in range(3):
            with open('../data/new_model_trainging/'+str(i)+'/'+name_list[a]+'.txt', 'w') as ff:
                for every in id_list[i]:
                    if extract_actor_comment(db, every) == 0:
                        print 'null'
                        continue
                    else:
                        print every
                        ff.write(write_score(db, every, a))
    # print extract_actor_comment(db,"588593")