#coding=utf-8

import jieba  # 结巴分词工具
import numpy as np
import lda

class WordSegemention:

    def __init__(self, sentence):
        self.sentence = sentence
        self.list_word = self.word_segment()

    def word_segment(self):

        """
        function:利用结巴分词工具分词
        :param sentence: 分词前的句原
        :return: list_word:分词后的词原
        """

        words = jieba.cut(self.sentence)
        list_word = list()
        for word in words:
            list_word.append(word)
        for word in list_word:
            if word.__len__() < 2:  # 去除标点符号
                list_word.remove(word)
        return list_word

    def get_words_with_segmention(self):
        return self.list_word


class LDAModel:

    def __init__(self, document_path, n_topics=10):

        """
        :param filename:文件录入
        录入样例:多行评论
            ---------
            这东西很好
            烂透了
            ----------
        """
        self.sentence_with_segment, self.vocabulary = self.load_document(document_path)
        self.vsm_model = self.train_vsm_model()
        self.n_topics = n_topics  # 主题的个数
        self.n_top_words = 10  # 取每个主题的前几个
        self.n_iter = 300  # 设置迭代的次数
        self.words_in_topic, self.perplexity = self.train_lda_model(self.n_topics)  # 获得lda主题模型下的词分布
        self.sum_topic_error = self.count_ste()

    @staticmethod
    def load_document(document_path):
        """
        function:加载数据
        :param document_path: 加载文件的路径
        :return:句子分词后词原列表的comments_with_segment, 词汇表vocabulary
        """
        sentence_with_segment = list()
        vocabulary = list()
        f = open(document_path, 'r')
        sentences = f.readlines()
        for sentence in sentences:
            #list_word = WordSegemention(sentence).get_words_with_segmention()
            list_word = sentence.split(" ")
            sentence_with_segment.append(list_word)
            for word in list_word:
                if vocabulary.count(word) == 0:
                    vocabulary.append(word)
        return sentence_with_segment, vocabulary

    def train_vsm_model(self):
        """
        function: 将词汇表训练成VSM模型, 权重为TF
        :return:
        """
        vsm_model = list()
        for sentence in self.sentence_with_segment:
            vsm = [ i*0 for i in range(0, self.vocabulary.__len__(), 1)]
            for word in sentence:
                index = self.vocabulary.index(word)
                vsm[index] += 1
            vsm_model.append(vsm)
        vsm_model = np.array(vsm_model)
        return vsm_model

    def train_lda_model(self, n_topics):
        """
        function: 训练LDA模型
        :param: n_topic: 主题的个数
        :return: words_in_topic 主题内的词分布
        """
        model = lda.LDA(n_topics=n_topics, n_iter= self.n_iter, random_state=1)
        model.fit(self.vsm_model)  # 填充vsm模型
        topic_word = model.topic_word_
        loglikelihood = model.loglikelihoods_
        perplexity = loglikelihood.pop() * (-1.0) / self.vocabulary.__len__() * self.n_topics
        n_top_words = self.n_top_words  # 取topic前几个热词
        words_in_topic = dict()
        for i, topic_dict in enumerate(topic_word):
            topic_words = np.array(self.vocabulary)[np.argsort(topic_dict)][:-(n_top_words+1):-1]
            words_in_topic[i] = topic_words
        return words_in_topic, perplexity

    # 得到主题的个数
    def get_topics(self):
        return self.words_in_topic

    # 得到复杂度
    def get_perplexity(self):
        return self.perplexity

    # 计算误差主题代价
    def count_ste(self):
        sum_topic_error = 0.0

        # 获得主题中出现的词语
        topic_vocabulary = []
        for j in self.words_in_topic:
            for word in self.words_in_topic[j]:
                if word not in topic_vocabulary:
                    topic_vocabulary.append(word)

        # 计算代价
        # for word in topic_vocabulary:
        #     sum = 0
        #     for j in self.words_in_topic:
        #         if word in self.words_in_topic[j]:
        #                 sum += 1
        #     sum_topic_error += (sum - 1)
        # # print sum_topic_error
        # print topic_vocabulary.__len__()
        # print self.words_in_topic.__len__()
        # print 'errro', 1.0 * sum_topic_error / topic_vocabulary.__len__()
        #
        # return 1.0 * self.words_in_topic.__len__() / sum_topic_error
        for i in self.words_in_topic:
            for j in self.words_in_topic:
                if i == j:
                    continue
            sum_topic_error += jaccard(self.words_in_topic[i], self.words_in_topic[j])
        print 1.0 * sum_topic_error / (self.words_in_topic.__len__() * 2)
        # print topic_vocabulary.__len__()
        # print self.words_in_topic.__len__()
        # print 'errro', 1.0 * sum_topic_error / topic_vocabulary.__len__()

        return 1.0 * sum_topic_error / (self.words_in_topic.__len__() * 2)


def jaccard(list_a, list_b):
    union = 0.0
    for i in list_a:
        if i in list_b:
            union += 1
    union /= list_a.__len__() + list_b.__len__() - union
    return union


def main():

    file_path = 'data/douban_comment_3434070Seg.txt'
    list_likelihood = []  # 记录每次迭代的复杂度
    list_ste = [] # 记录每次迭代中的损失主题代价
    list_topic = []   # 记录每次迭代的主题词语
    list_number = [8,9,10,11,12,13,14,15,16]
    list_error = []
    # list_number = [5,8,10,12]

    # 训练LDA
    for num in range(0, list_number.__len__(), 1):
        n_topic = list_number[num]
        lda = LDAModel(file_path, n_topics=n_topic)
        perplexity = lda.get_perplexity()
        topics = lda.get_topics()
        sum_topic_error = lda.count_ste()
        list_likelihood.append(perplexity)
        list_error.append(sum_topic_error)
        list_topic.append(topics)
        mycluster = MyCluster(topics, file_path)
        group = mycluster.get_group()
        valid_group = mycluster.count_valid_group()
        list_ste.append(valid_group)
        print group
        print valid_group
        # print perplexity
    print list_likelihood

    # 对复杂度归一化处理
    max_likelihood = 0
    min_likelihood = 9999999
    for one_likelihood in list_likelihood:
        if one_likelihood > max_likelihood:
            max_likelihood = one_likelihood
        if one_likelihood < min_likelihood:
            min_likelihood = one_likelihood
    for i in range(0, list_likelihood.__len__(), 1):
        list_likelihood[i] = 1.0 * (list_likelihood[i] - min_likelihood)/ (max_likelihood - min_likelihood + 0.1)

    # 对主题个数归一化处理
    max_number = 0
    min_number = 9999999
    for one_number in list_number:
        if one_number > max_number:
            max_number = one_number
        if one_number < min_number:
            one_number = min_number
    for i in range(0, list_number.__len__(), 1):
        list_number[i] = 1.0 * ( list_number[i] - min_number ) / ( max_number - min_number + 0.1)

    # 计算结构化风险并求出最小风险结果
    list_srm = []
    max = -1
    index = -1
    for i in range(0, list_ste.__len__(), 1):
        # srm = 0.2 * list_likelihood[i] + 0.8 * list_ste[i]
        srm = list_ste[i]
        list_srm.append(srm)
        if srm > max:
            max = srm
            index = i
    print list_srm

    # 输出最优主题词语分布
    topics = list_topic[index]
    for j in topics:
        print '------- topic %s ----------' % j
        for word in topics[j]:
            print word.replace("\n",""),
        print
    topic_file = file_path.replace(".txt", "topic.txt")
    write_topic(topics, topic_file)


def write_topic(topics, filename):
    f = open(filename, 'w')
    lines = ''
    num = 0
    for i in topics:
        line = str(num) + ':'
        for word in topics[i]:
            line += word + " "
        lines += line.replace("\n","").strip() + '\n'
        num += 1
    f.write(lines)
    f.close()


class MyCluster:
    def __init__(self, topic_dict, document_path):
        self.theta = 0.2
        self.topics = self.get_topic(topic_dict)
        self.comments = self.load_file(document_path)
        self.group = [ i* 0.0 for i in range(0, self.topics.__len__()+1, 1)]
        self.cluster()


    def get_topic(self, topic_dict):
        list_topic = []
        for i in topic_dict:
            list_topic.append(topic_dict[i])
        return list_topic

    def cluster(self):
        for comment in self.comments:
            comment_words = comment.split(" ")
            list_jaccard = [ i* 0.0 for i in range(0, self.topics.__len__(), 1)]
            for i in range(0, self.topics.__len__(), 1):
                topic_words = self.topics[i]
                jaccard = self.count_jaccard(comment_words, topic_words)
                list_jaccard[i] = jaccard
            max = 0
            index = -1
            for i in range(0, list_jaccard.__len__(), 1):
                if list_jaccard[i] >= max:
                    max = list_jaccard[i]
                    index = i
            if max < self.theta:
                self.group[self.topics.__len__()] += 1
            else:
                self.group[index] += 1

    def get_group(self):
        return self.group

    def count_valid_group(self):
        number = 0
        for i in self.group:
            if i > 20:
                number += 1
        return number-1

    def count_jaccard(self,list_a, list_b):
        union = 0.0
        for i in list_a:
            if i in list_b:
                union += 1
        if union >= 4:
            union /= list_a.__len__() + list_b.__len__() - union
        else:
            union = 0
        return union

    @staticmethod
    def load_file(file_path):
        f = open(file_path, 'r')
        lines = f.readlines()
        return lines



if __name__ == '__main__':
    print 'begin'
    main()
