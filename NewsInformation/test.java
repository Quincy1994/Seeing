

package movieCrawler;

public class test {
    public static void main(String[] args){
        String[] aa = {
                "美国队长3:内战",
                "惊天魔盗团2" ,
                "奇异博士" ,
                "x战警:天启" ,
                "功夫熊猫3" ,
                "疯狂动物城" ,
                "奇幻森林" ,
                "愤怒的小鸟" ,
                "爱宠大机密" ,
                "美人鱼" ,
                "湄公河行动" ,
                "澳门风云3" ,
                "绝地逃亡" ,
                "北京遇上西雅图之不二情书" ,
                "寒战2" ,
                "你的名字" ,
                "我不是潘金莲" ,
                "爵迹" ,
                "荒野猎人" ,
                "微微一笑很倾城" ,
                "王牌逗王牌" ,
                "谁的青春不迷茫" ,
                "七月与安生" ,
                "神探夏洛克:可恶的新娘" ,
                "追凶者也" ,
                "但丁密码" ,
                "鼠来宝4" ,
                "寄生兽" ,
                "笔仙诡影" ,
                "速度与激情7" ,
                "捉妖记" ,
                "港囧" ,
                "寻龙诀" ,
                "夏洛特烦恼" ,
                "侏罗纪世界" ,
                "煎饼侠" ,
                "澳门风云2" ,
                "西游记之大圣归来" ,
                "碟中谍5:神秘国度" ,
                "霍比特人3:五军之战" ,
                "天降雄狮" ,
                "终结者:创世纪" ,
                "老炮儿" ,
                "狼图腾" ,
                "九层妖塔" ,
                "蚁人" ,
                "恶棍天使" ,
                "火星救援",
                "007:幽灵党",
                "哆啦A梦:伴我同行"};
            movie_new a = new movie_new(aa[1]);
            movie_review b = new movie_review(aa[1]);
            statistics c  = new statistics(aa[1]);
            Save.saveMovie("C:\\Users\\49198\\IdeaProjects\\Movie\\src\\movieCrawler\\"+aa[1]+"_新闻.txt", a.newsContent);
            Save.saveMovie("C:\\Users\\49198\\IdeaProjects\\Movie\\src\\movieCrawler\\"+aa[1]+"_影评.txt", b.reviewsContent);
            Save.saveMovie("C:\\Users\\49198\\IdeaProjects\\Movie\\src\\movieCrawler\\"+aa[1]+"_票房统计.txt", c.statisticsContent);
    }
}
