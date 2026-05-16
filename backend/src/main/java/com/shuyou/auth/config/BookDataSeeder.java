package com.shuyou.auth.config;

import com.shuyou.auth.entity.Book;
import com.shuyou.auth.repository.BookRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// @Component
public class BookDataSeeder implements ApplicationRunner {
  private final BookRepository bookRepository;

  public BookDataSeeder(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    // 清理历史演示数据（旧版 code 以 book- 开头），避免和本次分类数据混杂。
    bookRepository.findAll().stream()
      .filter(book -> book.getCode() != null && book.getCode().startsWith("book-"))
      .forEach(bookRepository::delete);

    // 幂等导入：按固定 ID upsert，确保可重复启动/重复执行不产生重复数据。
    for (Book seed : seedBooks()) {
      Book target = bookRepository.findByCode(seed.getCode()).orElseGet(Book::new);
      target.setCode(seed.getCode());
      target.setTitle(seed.getTitle());
      target.setAuthor(seed.getAuthor());
      target.setTag(seed.getTag());
      target.setRating(seed.getRating());
      target.setReviews(seed.getReviews());
      target.setFeatured(seed.getFeatured());
      target.setCoverUrl(seed.getCoverUrl());
      target.setDescription(seed.getDescription());
      target.setPublishDate(seed.getPublishDate());
      target.setPublisher(seed.getPublisher());
      target.setPages(seed.getPages());
      target.setIsbn(seed.getIsbn());
      bookRepository.save(target);
    }
  }

  private List<Book> seedBooks() {
    return List.of(
      // 文学小说
      seed("lit-001", "红楼梦", "曹雪芹", "文学小说", 4.9, 1280, true, "hongloumeng.png", "中国古典长篇小说巅峰之作，描绘贾府兴衰与人物命运。", "1996-12", "人民文学出版社", 1600, "9787020002207"),
      seed("lit-002", "活着", "余华", "文学小说", 4.8, 1100, false, "huozhe.png", "通过福贵一生的遭际，呈现普通人在时代洪流中的生存韧性。", "2012-08", "作家出版社", 191, "9787506365437"),
      seed("lit-003", "围城", "钱钟书", "文学小说", 4.7, 720, false, "weicheng.jpg", "以幽默讽刺笔法书写知识分子的婚恋与社会处境。", "1991-02", "人民文学出版社", 359, "9787020090006"),
      seed("lit-004", "百年孤独", "加西亚·马尔克斯", "文学小说", 4.8, 1320, true, null, "魔幻现实主义代表作，讲述布恩迪亚家族七代人的历史循环。", "2011-06", "南海出版公司", 360, "9787544253994"),
      seed("lit-005", "追风筝的人", "卡勒德·胡赛尼", "文学小说", 4.7, 980, false, null, "围绕友谊、背叛与救赎展开的成长叙事。", "2006-05", "上海人民出版社", 362, "9781594631931"),

      // 历史传记
      seed("his-001", "史记", "司马迁", "历史传记", 4.9, 1050, true, null, "中国纪传体通史经典，奠定后世史书体例。", "2006-01", "中华书局", 3248, "9787101003048"),
      seed("his-002", "万历十五年", "黄仁宇", "历史传记", 4.8, 920, false, null, "从晚明关键年份切入，分析制度与历史走向。", "2006-08", "中华书局", 280, "9787101054491"),
      seed("his-003", "人类群星闪耀时", "斯蒂芬·茨威格", "历史传记", 4.7, 870, false, null, "以文学化笔法再现影响世界进程的历史瞬间。", "2004-10", "上海译文出版社", 356, "9787544702324"),
      seed("his-004", "苏东坡传", "林语堂", "历史传记", 4.7, 760, false, null, "讲述苏东坡的人生起伏与人格魅力。", "2014-01", "湖南文艺出版社", 416, "9787540475604"),
      seed("his-005", "拿破仑传", "艾米尔·路德维希", "历史传记", 4.6, 530, false, null, "欧洲传记经典，从政治与人格双线刻画拿破仑。", "2012-01", "华文出版社", 498, "9787511722676"),

      // 计算机编程
      seed("cs-001", "代码大全", "史蒂夫·迈克康奈尔", "计算机编程", 4.9, 1450, true, null, "软件构建实践经典，覆盖编码规范与工程细节。", "2011-05", "电子工业出版社", 914, "9780735619678"),
      seed("cs-002", "代码整洁之道", "罗伯特·C·马丁", "计算机编程", 4.8, 1320, false, null, "以可维护性为目标，系统阐述整洁代码原则。", "2010-01", "人民邮电出版社", 328, "9780132350884"),
      seed("cs-003", "深入理解计算机系统", "兰德尔·E·布莱恩特", "计算机编程", 4.9, 1180, false, null, "从程序到硬件层面解释计算机系统工作机制。", "2016-11", "机械工业出版社", 730, "9780134092669"),
      seed("cs-004", "算法导论", "托马斯·H·科尔曼", "计算机编程", 4.8, 980, false, null, "算法与数据结构权威教材，覆盖设计与复杂度分析。", "2013-01", "机械工业出版社", 780, "9780262033848"),
      seed("cs-005", "JavaScript高级程序设计", "尼古拉斯·C·扎卡斯", "计算机编程", 4.7, 860, false, null, "前端开发经典，系统梳理 JavaScript 核心机制与实践。", "2020-03", "人民邮电出版社", 720, "9781119366447"),

      // 心理学
      seed("psy-001", "思考，快与慢", "丹尼尔·卡尼曼", "心理学", 4.8, 1540, true, null, "行为经济学与认知心理学代表作，解释两套思维系统。", "2012-07", "中信出版社", 424, "9780374533557"),
      seed("psy-002", "被讨厌的勇气", "岸见一郎", "心理学", 4.7, 1380, false, null, "以阿德勒心理学解释课题分离与自我成长。", "2015-03", "机械工业出版社", 197, "9781501197277"),
      seed("psy-003", "自控力", "凯利·麦格尼格尔", "心理学", 4.7, 970, false, null, "结合神经科学与行为策略，提供可执行的自控训练方法。", "2012-08", "文化发展出版社", 263, "9781583335086"),
      seed("psy-004", "影响力", "罗伯特·B·西奥迪尼", "心理学", 4.8, 1260, false, null, "社会心理学经典，解析说服与决策中的关键机制。", "2010-09", "中国人民大学出版社", 320, "9780062937650"),
      seed("psy-005", "这才是心理学", "基思·斯坦诺维奇", "心理学", 4.6, 690, false, null, "澄清心理学研究方法与常见认知误区。", "2015-01", "人民邮电出版社", 352, "9780205914128"),

      // 经济学
      seed("eco-001", "国富论", "亚当·斯密", "经济学", 4.8, 920, true, null, "古典经济学奠基之作，讨论分工、市场与财富增长。", "2009-06", "商务印书馆", 560, "9780553585971"),
      seed("eco-002", "资本论", "卡尔·马克思", "经济学", 4.7, 860, false, null, "政治经济学经典，分析资本主义生产方式运行逻辑。", "2004-01", "人民出版社", 1000, "9780140445688"),
      seed("eco-003", "经济学原理", "N. 格里高利·曼昆", "经济学", 4.8, 1150, false, null, "入门经济学教材，覆盖微观与宏观核心框架。", "2020-01", "北京大学出版社", 760, "9780357133484"),
      seed("eco-004", "魔鬼经济学", "史蒂芬·列维特", "经济学", 4.6, 740, false, null, "用经济学思维解释看似无关的社会现象。", "2007-08", "中信出版社", 280, "9780060731335"),
      seed("eco-005", "贫穷的本质", "阿比吉特·班纳吉", "经济学", 4.7, 680, false, null, "基于发展经济学实证研究，探讨贫困与政策设计。", "2013-01", "中信出版社", 320, "9781610390934"),

      // 少儿绘本
      seed("kid-001", "猜猜我有多爱你", "山姆·麦克布雷尼", "少儿绘本", 4.9, 1580, true, null, "经典亲子绘本，以温暖对话表达爱与陪伴。", "2005-01", "明天出版社", 32, "9781406358780"),
      seed("kid-002", "好饿的毛毛虫", "艾瑞·卡尔", "少儿绘本", 4.9, 1490, false, null, "以拼贴画讲述毛毛虫成长过程，启发早期认知。", "2008-01", "明天出版社", 26, "9780399226908"),
      seed("kid-003", "大卫，不可以", "大卫·香农", "少儿绘本", 4.8, 930, false, null, "通过日常场景引导儿童建立行为边界。", "2007-01", "河北教育出版社", 32, "9780590930024"),
      seed("kid-004", "团圆", "余丽琼", "少儿绘本", 4.8, 760, false, null, "以中国春节为背景，描绘亲情与离合。", "2008-09", "明天出版社", 40, "9780761455196"),
      seed("kid-005", "活了100万次的猫", "佐野洋子", "少儿绘本", 4.8, 820, false, null, "通过寓言式故事表达生命与爱的意义。", "2004-01", "接力出版社", 32, "9784061272743"),

      // 科普科幻
      seed("sci-001", "时间简史", "史蒂芬·霍金", "科普科幻", 4.7, 1430, true, null, "面向大众介绍宇宙学与现代物理关键概念。", "2010-04", "湖南科学技术出版社", 248, "9780553380163"),
      seed("sci-002", "枪炮、病菌与钢铁", "贾雷德·戴蒙德", "科普科幻", 4.8, 860, false, null, "跨学科解释人类社会发展差异的成因。", "2006-04", "上海译文出版社", 532, "9780393354324"),
      seed("sci-003", "三体", "刘慈欣", "科普科幻", 4.9, 2380, false, null, "中国科幻代表作，探讨文明、科技与宇宙伦理。", "2008-01", "重庆出版社", 302, "9780765377067"),
      seed("sci-004", "沙丘", "弗兰克·赫伯特", "科普科幻", 4.8, 980, false, null, "史诗级科幻小说，融合政治、生态与宗教议题。", "2017-05", "江苏凤凰文艺出版社", 688, "9780441172719"),
      seed("sci-005", "基地", "艾萨克·阿西莫夫", "科普科幻", 4.8, 890, false, null, "银河帝国系列开篇，提出“心理史学”宏大设定。", "2005-09", "江苏文艺出版社", 280, "9780553293357"),

      // 管理学
      seed("mgt-001", "卓有成效的管理者", "彼得·德鲁克", "管理学", 4.8, 1280, true, null, "管理学经典，强调知识工作者的时间与成果管理。", "2009-09", "机械工业出版社", 208, "9780060833459"),
      seed("mgt-002", "从优秀到卓越", "吉姆·柯林斯", "管理学", 4.7, 860, false, null, "通过企业案例研究总结组织持续卓越的关键因素。", "2006-01", "中信出版社", 320, "9780066620992"),
      seed("mgt-003", "金字塔原理", "芭芭拉·明托", "管理学", 4.7, 910, false, null, "结构化表达经典方法，提升汇报与写作说服力。", "2013-09", "民主与建设出版社", 400, "9780273710516"),
      seed("mgt-004", "高效能人士的七个习惯", "史蒂芬·柯维", "管理学", 4.8, 1340, false, null, "个人与团队管理畅销书，强调原则导向的习惯养成。", "2010-10", "中国青年出版社", 354, "9780743269513"),
      seed("mgt-005", "领导梯队", "拉姆·查兰", "管理学", 4.6, 620, false, null, "围绕组织层级提出领导力发展模型与实践路径。", "2011-01", "机械工业出版社", 288, "9780787960759")
    );
  }

  private Book seed(String id,
                    String title,
                    String author,
                    String tag,
                    double rating,
                    int reviews,
                    boolean featured,
                    String coverFileName,
                    String description,
                    String publishDate,
                    String publisher,
                    Integer pages,
                    String isbn) {
    Book book = new Book();
    book.setCode(id);
    book.setTitle(title);
    book.setAuthor(author);
    book.setTag(tag);
    book.setRating(rating);
    book.setReviews(reviews);
    book.setFeatured(featured);
    // 使用占位图服务，避免本地文件缺失问题
    if (coverFileName != null && !coverFileName.isBlank()) {
      // 本地文件：使用内嵌的默认封面
      book.setCoverUrl("/photos/default-cover.jpg");
    } else if (isbn != null && !isbn.isBlank()) {
      // 使用占位图服务
      book.setCoverUrl("https://picsum.photos/seed/" + isbn + "/200/300");
    } else {
      book.setCoverUrl(null);
    }
    book.setDescription(description);
    book.setPublishDate(publishDate);
    book.setPublisher(publisher);
    book.setPages(pages);
    book.setIsbn(isbn);
    return book;
  }
}
