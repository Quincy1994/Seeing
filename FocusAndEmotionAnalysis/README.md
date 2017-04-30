### 关注点和情感分析
=====
原始数据格式： csv文件，movieId,score, time,comment</br>
</br>
首先通过 SplitMovieFile.java 把评论划分为以电影id为名的一个个文件</br>
</br>
EmotionAnalysis.java 日情感极性分析，读取id文件，引用 PathConfig.java 中的字典路径</br>
</br>
FocueAnalysis.java 关注点分析。</br>
</br>
mongoOper.java 把一部电影的关注点得分情况存入mongo数据库</br>
</br>
mysqlOper.java 向mysql中插入、更新数据（日平均分，日评论量，日情感分）</br>
</br>
PathConfig.java 字典路径</br>
