package com.shuyou.auth.config;

import com.shuyou.auth.entity.*;
import com.shuyou.auth.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
public class DataSeeder implements ApplicationRunner {
  private final UserAccountRepository userAccountRepository;
  private final BookRepository bookRepository;
  private final BookReviewRepository bookReviewRepository;
  private final BooklistRepository booklistRepository;
  private final ActivityRepository activityRepository;
  private final BooklistCommentRepository booklistCommentRepository;
  private final BooklistLikeRepository likeRepository;
  private final BooklistFollowRepository followRepository;
  private final BookReviewLikeRepository bookReviewLikeRepository;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final Random random = new Random();

  public DataSeeder(UserAccountRepository userAccountRepository,
                    BookRepository bookRepository,
                    BookReviewRepository bookReviewRepository,
                    BooklistRepository booklistRepository,
                    ActivityRepository activityRepository,
                    BooklistCommentRepository booklistCommentRepository,
                    BooklistLikeRepository likeRepository,
                    BooklistFollowRepository followRepository,
                    BookReviewLikeRepository bookReviewLikeRepository) {
    this.userAccountRepository = userAccountRepository;
    this.bookRepository = bookRepository;
    this.bookReviewRepository = bookReviewRepository;
    this.booklistRepository = booklistRepository;
    this.activityRepository = activityRepository;
    this.booklistCommentRepository = booklistCommentRepository;
    this.likeRepository = likeRepository;
    this.followRepository = followRepository;
    this.bookReviewLikeRepository = bookReviewLikeRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    seedBasicUsers();
    seedBasicBooks();
    seedBasicBooklists();
    seedBasicActivities();
  }

  private void seedUsers() {
    List<String> surnames = List.of("张", "李", "王", "赵", "刘", "陈", "杨", "黄", "周", "吴", "徐", "孙", "马", "朱", "胡", "郭", "何", "林", "罗", "高");
    List<String> names = List.of("伟", "强", "勇", "军", "杰", "敏", "静", "丽", "磊", "涛", "明", "芳", "华", "萍", "燕", "峰", "刚", "洋", "宁", "娜");
    List<String> bios = List.of(
      "热爱阅读的书友", "书评爱好者", "图书馆常客", "文学爱好者", "科幻迷", 
      "历史爱好者", "心理学爱好者", "经济学研究者", "管理类书籍爱好者",
      "少儿读物推广人", "艺术与美学爱好者", "科普爱好者", "漫画爱好者",
      "程序员读者", "大学生读者", "企业高管", "教师", "医生", "设计师", "自由职业者"
    );

    List<UserAccount> users = new ArrayList<>();
    users.add(createUser("zhangsan", "zhangsan@example.com", "张三", "热爱阅读的程序员，喜欢科技和文学", "13800138001"));
    users.add(createUser("lisi", "lisi@example.com", "李四", "书评人，专注于历史与哲学", "13800138002"));
    users.add(createUser("wangwu", "wangwu@example.com", "王五", "图书馆管理员，阅读爱好者", "13800138003"));
    users.add(createUser("zhaoliu", "zhaoliu@example.com", "赵六", "畅销书作家，著有《代码人生》", "13800138004"));
    users.add(createUser("sunqi", "sunqi@example.com", "孙七", "大学生，文学社团社长", "13800138005"));
    users.add(createUser("zhouba", "zhouba@example.com", "周八", "企业高管，管理类书籍爱好者", "13800138006"));
    users.add(createUser("wujiu", "wujiu@example.com", "吴九", "心理咨询师，心理学书籍推荐", "13800138007"));
    users.add(createUser("zhengshi", "zhengshi@example.com", "郑十", "科幻迷，刘慈欣忠实读者", "13800138008"));
    users.add(createUser("cheneleven", "chen@example.com", "陈十一", "经济学研究者，关注发展经济学", "13800138009"));
    users.add(createUser("liu12", "liu12@example.com", "刘十二", "少儿教育工作者，绘本推广人", "13800138010"));
    users.add(createUser("qian13", "qian13@example.com", "钱十三", "历史老师，热爱中国古典文学", "13800138011"));
    users.add(createUser("yang14", "yang14@example.com", "杨十四", "设计师，热爱艺术与美学书籍", "13800138012"));
    users.add(createUser("xu15", "xu15@example.com", "徐十五", "医生，医学科普爱好者", "13800138013"));
    users.add(createUser("sun16", "sun16@example.com", "孙十六", "学生，漫画和轻小说爱好者", "13800138014"));
    users.add(createUser("admin", "admin@shuyou.com", "系统管理员", "ShuYou 管理后台管理员账号", "13800138999", "ADMIN"));

    for (int i = 17; i <= 200; i++) {
      String surname = surnames.get(random.nextInt(surnames.size()));
      String name = names.get(random.nextInt(names.size()));
      String username = "user" + i;
      String email = "user" + i + "@example.com";
      String title = surname + name;
      String bio = bios.get(random.nextInt(bios.size()));
      String phone = "13800138" + String.format("%03d", i);
      users.add(createUser(username, email, title, bio, phone));
    }

    for (UserAccount user : users) {
      UserAccount existing = userAccountRepository.findByEmail(user.getEmail()).orElseGet(UserAccount::new);
      existing.setUsername(user.getUsername());
      existing.setEmail(user.getEmail());
      if (!existing.getEmail().equals("admin@shuyou.com") || existing.getPasswordHash() == null || existing.getPasswordHash().isEmpty()) {
        existing.setPasswordHash(passwordEncoder.encode("123456"));
      } else if (existing.getPasswordHash() == null || existing.getPasswordHash().isEmpty()) {
        existing.setPasswordHash(passwordEncoder.encode("admin123456"));
      }
      existing.setTitle(user.getTitle());
      existing.setBio(user.getBio());
      existing.setPhone(user.getPhone());
      existing.setRole(user.getRole());
      existing.setStatus(1);
      existing.setAvatarUrl("https://picsum.photos/seed/" + user.getUsername() + "/100/100");
      userAccountRepository.save(existing);
    }
  }

  private UserAccount createUser(String username, String email, String title, String bio, String phone) {
    return createUser(username, email, title, bio, phone, "USER");
  }

  private UserAccount createUser(String username, String email, String title, String bio, String phone, String role) {
    UserAccount user = new UserAccount();
    user.setUsername(username);
    user.setEmail(email);
    user.setTitle(title);
    user.setBio(bio);
    user.setPhone(phone);
    user.setRole(role);
    return user;
  }

  private void seedBooks() {
    bookRepository.findAll().stream()
      .filter(book -> book.getCode() != null && book.getCode().startsWith("book-"))
      .forEach(bookRepository::delete);

    List<Book> books = new ArrayList<>(List.of(
      seedBook("lit-001", "红楼梦", "曹雪芹", "文学小说", 4.9, 1280, true, "红楼梦.png", "中国古典长篇小说巅峰之作，描绘贾府兴衰与人物命运", "1996-12", "人民文学出版社", 1600, "9787020002207"),
      seedBook("lit-002", "活着", "余华", "文学小说", 4.8, 1100, true, "活着.png", "通过福贵一生的遭际，呈现普通人在时代洪流中的生存韧性", "2012-08", "作家出版社", 191, "9787506365437"),
      seedBook("lit-003", "围城", "钱钟书", "文学小说", 4.7, 720, false, "围城.jpg", "以幽默讽刺笔法书写知识分子的婚恋与社会处境", "1991-02", "人民文学出版社", 359, "9787020090006"),
      seedBook("lit-004", "百年孤独", "加西亚·马尔克斯", "文学小说", 4.8, 1320, true, "https://picsum.photos/seed/bainiangudu/200/300", "魔幻现实主义代表作，讲述布恩迪亚家族七代人的历史循环", "2011-06", "南海出版公司", 360, "9787544253994"),
      seedBook("lit-005", "追风筝的人", "卡勒德·胡赛尼", "文学小说", 4.7, 980, false, "https://picsum.photos/seed/zhuifengzheng/200/300", "围绕友谊、背叛与救赎展开的成长叙事", "2006-05", "上海人民出版社", 362, "9781594631931"),
      seedBook("lit-006", "平凡的世界", "路遥", "文学小说", 4.8, 1450, true, "https://picsum.photos/seed/pingfandeshijie/200/300", "全景式展现中国70年代中期到80年代中期城乡社会生活", "2012-03", "北京十月文艺出版社", 1265, "9787530212006"),
      seedBook("lit-007", "三体", "刘慈欣", "文学小说", 4.9, 2380, true, "https://picsum.photos/seed/santi/200/300", "地球文明与三体文明的史诗级碰撞，探讨文明存续的终极命题", "2008-01", "重庆出版社", 302, "9787536692930"),
      seedBook("lit-008", "1984", "乔治·奥威尔", "文学小说", 4.7, 860, false, "https://picsum.photos/seed/1984/200/300", "反乌托邦经典，警示极权主义对人性的摧残", "2010-04", "上海译文出版社", 320, "9780451524935"),
      seedBook("lit-009", "小王子", "安托万·德·圣-埃克苏佩里", "文学小说", 4.9, 1680, true, "https://picsum.photos/seed/xiaowangzi/200/300", "以童话形式探讨爱与生命的本质", "2003-08", "人民文学出版社", 96, "9780156012195"),
      seedBook("lit-010", "飘", "玛格丽特·米切尔", "文学小说", 4.8, 1120, false, "https://picsum.photos/seed/piao/200/300", "美国南北战争背景下的爱情史诗", "2000-09", "译林出版社", 1033, "9780446310789"),
      seedBook("lit-011", "西游记", "吴承恩", "文学小说", 4.9, 2100, true, "西游记.jpg", "中国古典四大名著之一，讲述师徒四人西天取经的故事", "1995-01", "人民文学出版社", 890, "9787020001234"),
      seedBook("lit-012", "三国演义", "罗贯中", "文学小说", 4.9, 1950, true, "三国演义.jpg", "中国古典四大名著之一，讲述东汉末年群雄逐鹿的历史演义", "1995-03", "人民文学出版社", 980, "9787020001241"),
      seedBook("lit-013", "水浒传", "施耐庵", "文学小说", 4.8, 1890, true, "水浒传.jpg", "中国古典四大名著之一，讲述梁山好汉聚义的故事", "1995-02", "人民文学出版社", 930, "9787020001258"),
      seedBook("lit-014", "老人与海", "欧内斯特·海明威", "文学小说", 4.7, 880, false, "老人与海.jpg", "一个老人与一条大鱼的搏斗，讲述勇气与毅力", "2008-01", "上海译文出版社", 152, "9787532744227"),
      seedBook("lit-015", "骆驼祥子", "老舍", "文学小说", 4.7, 760, false, "骆驼祥子.jpg", "讲述北平城人力车夫祥子的悲惨遭遇", "2012-01", "人民文学出版社", 240, "9787020038640"),
      seedBook("lit-016", "呐喊", "鲁迅", "文学小说", 4.8, 890, false, "呐喊.jpg", "鲁迅先生的第一本短篇小说集", "2006-01", "人民文学出版社", 198, "9787020002326"),
      seedBook("lit-017", "傲慢与偏见", "简·奥斯汀", "文学小说", 4.8, 1020, false, "傲慢与偏见.jpg", "一部描绘19世纪英国社会风俗的爱情小说", "2010-02", "上海译文出版社", 280, "9787532747702"),
      seedBook("lit-018", "双城记", "查尔斯·狄更斯", "文学小说", 4.7, 750, false, "双城记.jpg", "以法国大革命为背景的历史小说", "2009-01", "人民文学出版社", 320, "9787020038657"),
      seedBook("lit-019", "罪与罚", "陀思妥耶夫斯基", "文学小说", 4.8, 920, false, "罪与罚.jpg", "探讨犯罪与道德的经典之作", "2008-01", "上海译文出版社", 560, "9787532747000"),
      seedBook("lit-020", "安娜·卡列尼娜", "列夫·托尔斯泰", "文学小说", 4.9, 1150, true, "安娜・卡列尼娜.jpg", "俄国文学巅峰之作，讲述安娜的爱情悲剧", "2007-01", "人民文学出版社", 890, "9787020044131"),
      seedBook("lit-021", "变形记", "弗兰兹·卡夫卡", "文学小说", 4.7, 690, false, "变形记.jpg", "人变为虫的荒诞故事，探讨现代社会的异化", "2011-01", "上海译文出版社", 160, "9787532748807"),
      seedBook("lit-022", "追忆似水年华", "马塞尔·普鲁斯特", "文学小说", 4.8, 820, false, "追忆似水年华.jpg", "意识流文学的代表作", "2010-01", "译林出版社", 2100, "9787544710407"),
      seedBook("lit-023", "悲惨世界", "维克多·雨果", "文学小说", 4.9, 1280, true, "悲惨世界.jpg", "法国文学巨著，讲述爱与救赎的故事", "2006-01", "人民文学出版社", 1580, "9787020044148"),

      seedBook("his-001", "史记", "司马迁", "历史传记", 4.9, 1050, true, "https://picsum.photos/seed/shiji/200/300", "中国纪传体通史经典，奠定后世史书体例", "2006-01", "中华书局", 3248, "9787101003048"),
      seedBook("his-002", "万历十五年", "黄仁宇", "历史传记", 4.8, 920, false, "https://picsum.photos/seed/wanlishiwu/200/300", "从晚明关键年份切入，分析制度与历史走向", "2006-08", "中华书局", 280, "9787101054491"),
      seedBook("his-003", "人类群星闪耀时", "斯蒂芬·茨威格", "历史传记", 4.7, 870, false, "https://picsum.photos/seed/renlequnxing/200/300", "以文学化笔法再现影响世界进程的历史瞬间", "2004-10", "上海译文出版社", 356, "9787544702324"),
      seedBook("his-004", "苏东坡传", "林语堂", "历史传记", 4.7, 760, false, "https://picsum.photos/seed/sudongpo/200/300", "讲述苏东坡的人生起伏与人格魅力", "2014-01", "湖南文艺出版社", 416, "9787540475604"),
      seedBook("his-005", "明朝那些事儿", "当年明月", "历史传记", 4.8, 1890, true, "https://picsum.photos/seed/mingchaonaxieshi/200/300", "以通俗笔法讲述明朝三百年历史", "2017-05", "浙江人民出版社", 2184, "9787213080638"),
      seedBook("his-006", "罗马人的故事", "盐野七生", "历史传记", 4.7, 680, false, "https://picsum.photos/seed/luomadegushi/200/300", "15册巨著，全景式展现罗马帝国千年兴衰", "2012-11", "中信出版社", 416, "9787508633818"),
      seedBook("his-007", "全球通史", "斯塔夫里阿诺斯", "历史传记", 4.8, 1050, false, "https://picsum.photos/seed/quanqiutongshi/200/300", "从史前文明到现代世界的全球视角历史", "2006-01", "北京大学出版社", 723, "9780131815601"),

      seedBook("cs-001", "代码大全", "史蒂夫·迈克康奈尔", "计算机编程", 4.9, 1450, true, "https://picsum.photos/seed/daidaquan/200/300", "软件构建实践经典，覆盖编码规范与工程细节", "2011-05", "电子工业出版社", 914, "9780735619678"),
      seedBook("cs-002", "代码整洁之道", "罗伯特·C·马丁", "计算机编程", 4.8, 1320, false, "https://picsum.photos/seed/daidazhengjie/200/300", "以可维护性为目标，系统阐述整洁代码原则", "2010-01", "人民邮电出版社", 328, "9780132350884"),
      seedBook("cs-003", "深入理解计算机系统", "兰德尔·E·布莱恩特", "计算机编程", 4.9, 1180, false, "https://picsum.photos/seed/shenrulijie/200/300", "从程序到硬件层面解释计算机系统工作机制", "2016-11", "机械工业出版社", 730, "9780134092669"),
      seedBook("cs-004", "算法导论", "托马斯·H·科尔曼", "计算机编程", 4.8, 980, false, "https://picsum.photos/seed/suanfadaolun/200/300", "算法与数据结构权威教材，覆盖设计与复杂度分析", "2013-01", "机械工业出版社", 780, "9780262033848"),
      seedBook("cs-005", "设计模式", "Erich Gamma", "计算机编程", 4.8, 1120, false, "https://picsum.photos/seed/shejimoshi/200/300", "软件开发设计模式经典著作", "2010-09", "机械工业出版社", 395, "9780201633610"),
      seedBook("cs-006", "人月神话", "弗雷德里克·布鲁克斯", "计算机编程", 4.7, 760, false, "https://picsum.photos/seed/renyueshenhua/200/300", "软件工程领域经典，探讨项目管理本质", "2007-06", "清华大学出版社", 230, "9780201006506"),
      seedBook("cs-007", "图解HTTP", "上野宣", "计算机编程", 4.6, 580, false, "https://picsum.photos/seed/tujiehttp/200/300", "以图解方式讲解HTTP协议原理", "2014-08", "人民邮电出版社", 240, "9787115351531"),

      seedBook("psy-001", "思考，快与慢", "丹尼尔·卡尼曼", "心理学", 4.8, 1540, true, "https://picsum.photos/seed/sikaokuaiman/200/300", "行为经济学与认知心理学代表作，解释两套思维系统", "2012-07", "中信出版社", 424, "9780374533557"),
      seedBook("psy-002", "被讨厌的勇气", "岸见一郎", "心理学", 4.7, 1380, false, "https://picsum.photos/seed/beitaoyande/200/300", "以阿德勒心理学解释课题分离与自我成长", "2015-03", "机械工业出版社", 197, "9781501197277"),
      seedBook("psy-003", "自控力", "凯利·麦格尼格尔", "心理学", 4.7, 970, false, "https://picsum.photos/seed/zikongli/200/300", "结合神经科学与行为策略，提供可执行的自控训练方法", "2012-08", "文化发展出版社", 263, "9781583335086"),
      seedBook("psy-004", "影响力", "罗伯特·B·西奥迪尼", "心理学", 4.8, 1260, false, "https://picsum.photos/seed/yingxiangli/200/300", "社会心理学经典，解析说服与决策中的关键机制", "2010-09", "中国人民大学出版社", 320, "9780062937650"),
      seedBook("psy-005", "自卑与超越", "阿尔弗雷德·阿德勒", "心理学", 4.6, 620, false, "https://picsum.photos/seed/zibeichaoyue/200/300", "个体心理学创始人阿德勒的代表作", "2015-07", "浙江人民出版社", 184, "9780465051157"),
      seedBook("psy-006", "非暴力沟通", "马歇尔·卢森堡", "心理学", 4.7, 890, false, "https://picsum.photos/seed/feibaoli/200/300", "教会人们如何建立真诚的沟通", "2009-01", "华夏出版社", 190, "9781882005134"),

      seedBook("eco-001", "国富论", "亚当·斯密", "经济学", 4.8, 920, true, "https://picsum.photos/seed/guofulun/200/300", "古典经济学奠基之作，讨论分工、市场与财富增长", "2009-06", "商务印书馆", 560, "9780553585971"),
      seedBook("eco-002", "经济学原理", "N. 格里高利·曼昆", "经济学", 4.8, 1150, false, "https://picsum.photos/seed/jingjixueyuanli/200/300", "入门经济学教材，覆盖微观与宏观核心框架", "2020-01", "北京大学出版社", 760, "9780357133484"),
      seedBook("eco-003", "魔鬼经济学", "史蒂芬·列维特", "经济学", 4.6, 740, false, "https://picsum.photos/seed/moguijingji/200/300", "用经济学思维解释看似无关的社会现象", "2007-08", "中信出版社", 280, "9780060731335"),
      seedBook("eco-004", "贫穷的本质", "阿比吉特·班纳吉", "经济学", 4.7, 680, false, "https://picsum.photos/seed/pinmian/200/300", "基于发展经济学实证研究，探讨贫困与政策设计", "2013-01", "中信出版社", 320, "9781610390934"),
      seedBook("eco-005", "薛兆丰经济学讲义", "薛兆丰", "经济学", 4.6, 1020, false, "https://picsum.photos/seed/xuezhaofeng/200/300", "用通俗语言讲解经济学原理", "2018-07", "中信出版社", 480, "9787508689586"),

      seedBook("kid-001", "猜猜我有多爱你", "山姆·麦克布雷尼", "少儿绘本", 4.9, 1580, true, "https://picsum.photos/seed/caicaiyou/200/300", "经典亲子绘本，以温暖对话表达爱与陪伴", "2005-01", "明天出版社", 32, "9781406358780"),
      seedBook("kid-002", "好饿的毛毛虫", "艾瑞·卡尔", "少儿绘本", 4.9, 1490, false, "https://picsum.photos/seed/haoemaomao/200/300", "以拼贴画讲述毛毛虫成长过程，启发早期认知", "2008-01", "明天出版社", 26, "9780399226908"),
      seedBook("kid-003", "大卫，不可以", "大卫·香农", "少儿绘本", 4.8, 930, false, "https://picsum.photos/seed/dawei/200/300", "通过日常场景引导儿童建立行为边界", "2007-01", "河北教育出版社", 32, "9780590930024"),
      seedBook("kid-004", "团圆", "余丽琼", "少儿绘本", 4.8, 760, false, "https://picsum.photos/seed/tuanyuan/200/300", "以中国春节为背景，描绘亲情与离合", "2008-09", "明天出版社", 40, "9780761455196"),
      seedBook("kid-005", "小熊温尼", "A.A.米尔恩", "少儿绘本", 4.7, 680, false, "https://picsum.photos/seed/xiaoxiong/200/300", "小熊温尼和朋友们的温馨故事", "2013-01", "译林出版社", 128, "9780140631078"),

      seedBook("sci-001", "时间简史", "史蒂芬·霍金", "科普科幻", 4.7, 1430, true, "https://picsum.photos/seed/shijianjian/200/300", "面向大众介绍宇宙学与现代物理关键概念", "2010-04", "湖南科学技术出版社", 248, "9780553380163"),
      seedBook("sci-002", "枪炮、病菌与钢铁", "贾雷德·戴蒙德", "科普科幻", 4.8, 860, false, "https://picsum.photos/seed/qiangpaobing/200/300", "跨学科解释人类社会发展差异的成因", "2006-04", "上海译文出版社", 532, "9780393354324"),
      seedBook("sci-003", "沙丘", "弗兰克·赫伯特", "科普科幻", 4.8, 980, false, "https://picsum.photos/seed/shachiu/200/300", "史诗级科幻小说，融合政治、生态与宗教议题", "2017-05", "江苏凤凰文艺出版社", 688, "9780441172719"),
      seedBook("sci-004", "基地", "艾萨克·阿西莫夫", "科普科幻", 4.8, 890, false, "https://picsum.photos/seed/jidi/200/300", "银河帝国系列开篇，提出心理史学宏大设定", "2005-09", "江苏文艺出版社", 280, "9780553293357"),
      seedBook("sci-005", "流浪地球", "刘慈欣", "科普科幻", 4.7, 520, false, "https://picsum.photos/seed/liulangdiqiu/200/300", "太阳即将毁灭，人类开启星际流浪之旅", "2019-01", "长江文艺出版社", 304, "9787570206372"),

      seedBook("mgt-001", "卓有成效的管理者", "彼得·德鲁克", "管理学", 4.8, 1280, true, "https://picsum.photos/seed/zhuoyouxiao/200/300", "管理学经典，强调知识工作者的时间与成果管理", "2009-09", "机械工业出版社", 208, "9780060833459"),
      seedBook("mgt-002", "从优秀到卓越", "吉姆·柯林斯", "管理学", 4.7, 860, false, "https://picsum.photos/seed/congyouxiu/200/300", "通过企业案例研究总结组织持续卓越的关键因素", "2006-01", "中信出版社", 320, "9780066620992"),
      seedBook("mgt-003", "金字塔原理", "芭芭拉·明托", "管理学", 4.7, 910, false, "https://picsum.photos/seed/jinziyuanli/200/300", "结构化表达经典方法，提升汇报与写作说服力", "2013-09", "民主与建设出版社", 400, "9780273710516"),
      seedBook("mgt-004", "高效能人士的七个习惯", "史蒂芬·柯维", "管理学", 4.8, 1340, false, "https://picsum.photos/seed/gaoxiaoneng/200/300", "个人与团队管理畅销书，强调原则导向的习惯养成", "2010-10", "中国青年出版社", 354, "9780743269513"),
      seedBook("mgt-005", "精益创业", "埃里克·莱斯", "管理学", 4.6, 780, false, "https://picsum.photos/seed/jingyi/200/300", "指导创业者如何用最小可行产品验证商业假设", "2012-08", "中信出版社", 256, "9780307887894")
    ));

    for (Book seed : books) {
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
      target.setLanguage(seed.getLanguage());
      target.setCategory(seed.getCategory());
      target.setLongDescription(seed.getLongDescription());
      target.setAuthorIntro(seed.getAuthorIntro());
      bookRepository.save(target);
    }
  }

  private Book seedBook(String id, String title, String author, String tag, double rating,
                        int reviews, boolean featured, String coverFileName, String description,
                        String publishDate, String publisher, Integer pages, String isbn) {
    Book book = new Book();
    book.setCode(id);
    book.setTitle(title);
    book.setAuthor(author);
    book.setTag(tag);
    book.setRating(rating);
    book.setReviews(reviews);
    book.setFeatured(featured);
    if (coverFileName != null && !coverFileName.isBlank() && (coverFileName.endsWith(".png") || coverFileName.endsWith(".jpg"))) {
      book.setCoverUrl("/photos/" + coverFileName);
    } else if (coverFileName != null && !coverFileName.isBlank() && coverFileName.startsWith("http")) {
      book.setCoverUrl(coverFileName);
    } else if (isbn != null && !isbn.isBlank()) {
      book.setCoverUrl("https://picsum.photos/seed/" + isbn + "/200/300");
    } else {
      book.setCoverUrl("https://picsum.photos/seed/" + title + "/200/300");
    }
    book.setDescription(description);
    book.setPublishDate(publishDate);
    book.setPublisher(publisher);
    book.setPages(pages);
    book.setIsbn(isbn);
    book.setLanguage("中文");
    book.setCategory(mapTagToCategory(tag));
    book.setLongDescription(buildLongDescription(title));
    book.setAuthorIntro(buildAuthorIntro(author));
    return book;
  }

  private String mapTagToCategory(String tag) {
    return tag;
  }

  private String buildLongDescription(String title) {
    return switch (title) {
      case "红楼梦" -> "《红楼梦》是清代作家曹雪芹创作的章回体长篇小说，中国古典四大名著之一。小说以贾、史、王、薛四大家族的兴衰为背景，以贾宝玉、林黛玉、薛宝钗的爱情婚姻悲剧为主线，描绘了一批举止见识出于须眉之上的闺阁佳人的人生百态，展现了真正的人性美和悲剧美，可以说是一部从各个角度展现中国古代社会百态的史诗性著作。全书规模宏大，人物众多，结构严谨，语言优美，被誉为中国古典小说的巅峰之作。";
      case "活着" -> "《活着》是作家余华的代表作之一，讲述了农村人福贵悲惨的人生遭遇。福贵本是富家少爷，却因赌博输光家产，父亲被气死，自己沦为佃农。随后，他经历了抗日战争、解放战争、大跃进、文化大革命等一系列历史事件，目睹了女儿、儿子、妻子、孙子一个个离他而去。小说通过福贵的一生，深刻揭示了命运的残酷和生命的坚韧，展现了普通人在时代洪流中的生存状态和对生命的执着。";
      case "三体" -> "《三体》是刘慈欣创作的长篇科幻小说，是中国科幻的里程碑之作。小说讲述了地球文明与三体文明的首次接触及其相互作用的过程。三体文明因母星环境恶化而寻找新家园，发现地球后决定入侵。人类社会在得知这一消息后，陷入了前所未有的危机。小说通过宏大的叙事，探讨了文明的本质、宇宙的黑暗森林法则、技术爆炸等深刻的哲学问题，展现了人类面对未知威胁时的勇气、智慧和挣扎。";
      case "百年孤独" -> "《百年孤独》是哥伦比亚作家加西亚·马尔克斯的代表作，魔幻现实主义文学的巅峰之作。小说描写了布恩迪亚家族七代人的传奇故事，以及马孔多小镇的兴衰历程。作品融入了神话传说、民间故事、宗教典故等神秘因素，巧妙地糅合了现实与虚幻，展现出一个瑰丽的想象世界，反映了拉丁美洲一个世纪以来风云变幻的历史。";
      case "平凡的世界" -> "《平凡的世界》是路遥创作的长篇小说，全景式地展现了中国70年代中期到80年代中期城乡社会生活。小说以孙少安和孙少平兄弟为中心，刻画了当时社会各阶层众多普通人的形象，深刻地展示了普通人在大时代历史进程中所走过的艰难曲折的道路。";
      case "围城" -> "《围城》是钱钟书先生的代表作，被誉为'新儒林外史'。小说以方鸿渐为主人公，描写了他从欧洲留学回国后的生活经历，包括他的爱情、婚姻和事业。钱钟书以其独特的幽默和讽刺笔法，描绘了抗战时期中国知识分子的生活百态和精神困境，揭示了人性的弱点和社会的弊病。";
      case "小王子" -> "《小王子》是法国作家安托万·德·圣-埃克苏佩里创作的一部童话小说。故事讲述了一位来自小行星的小王子，在与玫瑰花闹别扭后，开始了在宇宙中的旅行。他先后访问了六个星球，遇见了国王、虚荣的人、商人、点灯人、地理学家等奇怪的人，最后来到地球，在沙漠中与飞行员相遇。小说通过小王子的故事，探讨了爱、友谊、责任、成长等深刻的主题，语言优美，寓意深远。";
      case "1984" -> "《1984》是乔治·奥威尔创作的反乌托邦经典小说。故事发生在一个极权主义统治的国家'大洋国'，主人公温斯顿是一名外围党员，他对党的统治产生了怀疑，并开始秘密反抗。小说描绘了一个充满监控、谎言和思想控制的恐怖世界，深刻警示了极权主义对人性的摧残和对自由的压制。";
      case "史记" -> "《史记》是西汉史学家司马迁撰写的纪传体史书，是中国历史上第一部纪传体通史。全书共一百三十篇，记载了从上古传说中的黄帝时代到汉武帝太初年间共三千多年的历史。《史记》不仅是一部伟大的史学著作，也是一部优秀的文学作品，其文辞优美，人物刻画生动，对后世史学和文学都产生了深远影响。";
      case "明朝那些事儿" -> "《明朝那些事儿》是当年明月创作的历史通俗读物。作者以史料为基础，以年代和具体人物为主线，全景式地展现了明朝两百多年的历史。作品用生动活泼的语言，讲述了明朝的政治、经济、文化等各个方面的故事，使读者能够轻松地了解明朝的历史。";
      case "代码大全" -> "《代码大全》是史蒂夫·迈克康奈尔创作的软件开发经典著作。本书涵盖了软件构建的各个方面，包括编码规范、设计原则、调试技巧、性能优化等内容。书中提供了大量实用的建议和最佳实践，帮助开发者编写高质量的代码，是软件开发人员必读的经典之作。";
      case "思考，快与慢" -> "《思考，快与慢》是诺贝尔经济学奖得主丹尼尔·卡尼曼的代表作。作者在书中介绍了人类思维的两种模式：快思考和慢思考。快思考是直觉的、快速的、无意识的，而慢思考是理性的、缓慢的、有意识的。通过大量的实验和案例，卡尼曼揭示了这两种思维模式的特点和局限性，帮助读者更好地理解自己的思维过程，做出更明智的决策。";
      case "经济学原理" -> "《经济学原理》是曼昆创作的经济学教科书。本书以清晰易懂的语言，介绍了经济学的基本原理和概念，包括供给与需求、弹性、市场结构、宏观经济政策等内容。书中结合了大量的实例和案例，帮助读者理解经济学在现实生活中的应用，是学习经济学的入门佳作。";
      default -> "";
    };
  }

  private String buildAuthorIntro(String author) {
    return switch (author) {
      case "曹雪芹" -> "曹雪芹（约1715—约1763），清代小说家，名霑，字梦阮，号雪芹。他出身于清代内务府正白旗包衣世家，祖父曹寅曾担任江宁织造。家族曾显赫一时，但后来家道中落。曹雪芹历经人生沧桑，晚年创作了《红楼梦》这部不朽的文学巨著。";
      case "余华" -> "余华，1960年出生于浙江杭州，中国当代著名作家。他的作品以其独特的叙事风格和深刻的社会洞察力著称。代表作有《活着》《许三观卖血记》《兄弟》等，作品被翻译成多种语言，在国际上享有盛誉。";
      case "刘慈欣" -> "刘慈欣，1963年出生于北京，中国科幻小说作家，被誉为'中国科幻第一人'。他的作品《三体》系列开创了中国科幻文学的新纪元，获得了多项国内外大奖。刘慈欣的作品以宏大的视野、深刻的思想和精彩的故事著称。";
      case "加西亚·马尔克斯" -> "加夫列尔·加西亚·马尔克斯（1927—2014），哥伦比亚作家，1982年诺贝尔文学奖得主。他是魔幻现实主义文学的代表人物，代表作《百年孤独》《霍乱时期的爱情》等作品被翻译成多种语言，在全球范围内产生了深远影响。";
      case "路遥" -> "路遥（1949—1992），中国当代作家，原名王卫国。他的作品《平凡的世界》以其深刻的社会洞察力和真挚的情感描写，展现了普通人在大时代中的奋斗与成长，成为中国文学的经典之作。";
      case "钱钟书" -> "钱钟书（1910—1998），中国现代著名作家、文学研究家。他精通多种语言，学识渊博，著有《围城》《管锥编》等作品。钱钟书以其独特的幽默和深刻的思想著称，是中国现代文学史上的重要人物。";
      case "司马迁" -> "司马迁（约前145—约前87），西汉史学家、文学家。他继承父业，担任太史令，历时十余年完成了《史记》这部伟大的史学著作。司马迁以其'究天人之际，通古今之变，成一家之言'的精神，为后世留下了宝贵的历史遗产。";
      case "当年明月" -> "当年明月，本名石悦，1979年出生于湖北宜昌。他以《明朝那些事儿》一书成名，用通俗幽默的语言讲述历史，使历史变得生动有趣，深受读者喜爱。";
      case "丹尼尔·卡尼曼" -> "丹尼尔·卡尼曼，1934年出生于以色列，诺贝尔经济学奖得主。他是行为经济学的奠基人之一，通过研究人类的决策行为，揭示了很多传统经济学无法解释的现象，对经济学和心理学都产生了深远影响。";
      default -> "";
    };
  }

  private void seedBookReviews() {
    Map<String, List<String>> shortReviewsByCategory = Map.of(
      "文学小说", List.of(
        "👍 经典永流传！",
        "人物刻画太绝了",
        "哭了😭",
        "结局意难平",
        "文字太美了",
        "后劲好大",
        "百看不厌",
        "相见恨晚",
        "宝藏好书",
        "必须五星",
        "名不虚传",
        "强烈安利",
        "看完沉默",
        "回味无穷",
        "爱了爱了"
      ),
      "历史传记", List.of(
        "历史的魅力！",
        "涨知识了",
        "原来如此",
        "大开眼界",
        "受益匪浅",
        "值得一读",
        "经典！",
        "干货满满",
        "刷新认知",
        "精彩！",
        "推荐！",
        "五星好评",
        "太精彩了",
        "收获颇丰",
        "历史迷必看"
      ),
      "计算机编程", List.of(
        "代码写得更优雅了",
        "醍醐灌顶！",
        "受益匪浅",
        "架构师必读",
        "代码规范指南",
        "面试加分",
        "进阶必备",
        "深入浅出",
        "干货满满",
        "强烈推荐",
        "五星好评",
        "经典！",
        "受益匪浅",
        "必读书单",
        "程序员圣经"
      ),
      "心理学", List.of(
        "豁然开朗！",
        "治愈了我",
        "了解自己",
        "人际关系",
        "情绪管理",
        "认知升级",
        "思维改变",
        "自我成长",
        "受益匪浅",
        "推荐！",
        "五星好评",
        "宝藏书",
        "相见恨晚",
        "心理学入门",
        "实用！"
      ),
      "经济学", List.of(
        "经济学思维",
        "理性决策",
        "看懂世界",
        "深入浅出",
        "经济学入门",
        "思维模型",
        "刷新认知",
        "推荐！",
        "五星好评",
        "经典！",
        "受益匪浅",
        "经济学原理",
        "看懂经济",
        "必读！",
        "涨知识"
      ),
      "少儿绘本", List.of(
        "亲子共读",
        "温暖治愈",
        "孩子超爱",
        "画面精美",
        "启蒙读物",
        "寓教于乐",
        "温馨感人",
        "可爱！",
        "推荐！",
        "五星好评",
        "绘本经典",
        "睡前故事",
        "童真童趣",
        "孩子喜欢",
        "值得收藏"
      ),
      "科普科幻", List.of(
        "脑洞大开！",
        "宇宙的奥秘",
        "科学之美",
        "硬核科普",
        "科幻经典",
        "想象力爆炸",
        "震撼！",
        "涨知识了",
        "推荐！",
        "五星好评",
        "经典！",
        "大开眼界",
        "科学入门",
        "硬核！",
        "必读！"
      )
    );

    Map<String, List<String>> mediumReviewsByCategory = Map.of(
      "文学小说", List.of(
        "真的太好看了！熬夜看完的，结局让我哭了好久",
        "人物刻画太立体了，每个人都有血有肉",
        "作者的文笔真的太美了，每句话都是享受",
        "看了一半就迫不及待想分享给朋友",
        "已经是第二遍读了，每次都有新发现",
        "终于明白为什么是经典了",
        "书里的情感描写太细腻了，完全代入",
        "文字很温暖，治愈了我最近的焦虑",
        "一口气读完，根本停不下来！",
        "故事线很清晰，伏笔埋得很好",
        "每个角色都让人印象深刻",
        "看完书还一直在回味，后劲真的很大",
        "文笔细腻，情感真挚，真的很感动",
        "情节紧凑，一环扣一环，非常过瘾",
        "书中的道理很深刻，值得反复品味",
        "故事很温暖，治愈了我最近的坏心情",
        "作者的文字功底很好，读起来很舒服",
        "和朋友讨论了好久书里的人物和情节",
        "很久没读到这么让人感动的书了",
        "语言很有感染力，让我仿佛身临其境"
      ),
      "历史传记", List.of(
        "读完对这段历史有了全新的认识",
        "作者用通俗的语言讲清楚了复杂的历史",
        "原来历史可以这么有趣，涨知识了",
        "从书中看到了很多历史人物的另一面",
        "以史为鉴，可以知兴替",
        "这本书让我爱上了历史",
        "史料详实，分析透彻",
        "作者的观点很新颖，刷新了我的认知",
        "读完对人性有了更深的理解",
        "历史的细节原来这么有意思",
        "推荐给所有对历史感兴趣的朋友",
        "这本书改变了我对历史的看法",
        "每个历史人物都刻画得很生动",
        "从历史中学到了很多做人做事的道理",
        "作者的研究很深入，受益匪浅",
        "历史不是枯燥的年份，而是鲜活的故事",
        "这本书让我重新认识了历史",
        "原来历史可以这么精彩",
        "强烈推荐给历史爱好者",
        "从书中看到了时代的变迁"
      ),
      "计算机编程", List.of(
        "看完这本书，代码写得更优雅了",
        "作者把复杂的概念讲得通俗易懂",
        "这本书让我的编程能力提升了一个档次",
        "每个程序员都应该读的一本书",
        "书中的原则让我的代码质量提升很多",
        "设计模式讲得很透彻，受益匪浅",
        "算法讲解很清晰，面试前看很有帮助",
        "这本书帮我解决了很多实际工作中的问题",
        "架构设计的思路让我豁然开朗",
        "代码规范方面学到了很多",
        "这本书是我读过最好的编程书籍",
        "深入浅出，容易理解",
        "每个章节都有干货，值得反复阅读",
        "书中的例子很实用，可以直接用到项目中",
        "这本书让我对编程有了新的理解",
        "强烈推荐给想要进阶的程序员",
        "代码整洁之道，名不虚传",
        "这本书改变了我的编程思维",
        "算法导论，程序员必备",
        "设计模式让我的代码更优雅"
      ),
      "心理学", List.of(
        "这本书让我更了解自己了",
        "豁然开朗，很多困惑都解开了",
        "书中的方法很实用，可以直接用到生活中",
        "人际关系方面学到了很多",
        "情绪管理的技巧很有用",
        "认知升级，思维改变",
        "自我成长的好书，推荐！",
        "这本书治愈了我很多",
        "学会了更好地处理情绪",
        "人际关系变得更和谐了",
        "这本书改变了我的思维方式",
        "心理学入门的好书",
        "了解自己，接纳自己",
        "书中的观点很有启发性",
        "学会了非暴力沟通，受益匪浅",
        "自控力提升了很多",
        "影响力这本书让我看清了很多",
        "被讨厌的勇气，让我更自信了",
        "思考快与慢，了解自己的思维",
        "自卑与超越，自我成长"
      ),
      "经济学", List.of(
        "经济学思维让我看问题更透彻了",
        "原来经济学可以这么有趣",
        "书中的原理可以解释很多社会现象",
        "理性决策的能力提升了",
        "看懂了很多经济现象背后的逻辑",
        "经济学入门的好书，推荐！",
        "作者用通俗的语言讲清楚了经济学",
        "思维模型很有用，可以用到很多地方",
        "这本书改变了我的消费观念",
        "看懂了供给和需求的关系",
        "经济学原理讲得很透彻",
        "魔鬼经济学让我看到了不一样的视角",
        "贫穷的本质，引人深思",
        "国富论，经济学的经典",
        "薛兆丰的经济学讲义很接地气",
        "这本书让我对经济有了全新的认识",
        "经济学思维帮助我做出更好的决策",
        "推荐给所有想了解经济学的人",
        "深入浅出，容易理解",
        "这本书让我受益匪浅"
      ),
      "少儿绘本", List.of(
        "亲子共读的好书，孩子很喜欢",
        "画面精美，故事温馨",
        "孩子每次都要读好几遍",
        "绘本中的道理很适合孩子理解",
        "启蒙读物的好选择",
        "寓教于乐，孩子学得很开心",
        "故事很温馨，大人读了也很感动",
        "画面色彩鲜艳，吸引孩子注意力",
        "睡前故事的最佳选择",
        "这本书让孩子爱上了阅读",
        "绘本中的价值观很正",
        "孩子从中学到了很多道理",
        "亲子互动的好帮手",
        "画面很有艺术感",
        "故事简单但很有深意",
        "孩子的第一本绘本，很满意",
        "温馨感人，值得收藏",
        "推荐给有孩子的朋友",
        "绘本经典，值得拥有",
        "孩子超爱，读了无数遍"
      ),
      "科普科幻", List.of(
        "脑洞大开，想象力爆炸！",
        "宇宙的奥秘太神奇了",
        "科学原来可以这么有趣",
        "硬核科普，涨知识了",
        "科幻经典，百看不厌",
        "作者的想象力太丰富了",
        "书中的设定很严谨",
        "看完对科学更感兴趣了",
        "推荐给所有科幻迷",
        "这本书让我爱上了科普",
        "时间简史，经典中的经典",
        "枪炮病菌与钢铁，视角独特",
        "沙丘，科幻史诗",
        "基地系列，太精彩了",
        "流浪地球，中国科幻的骄傲",
        "作者构建的世界观很完整",
        "科学知识和故事结合得很好",
        "这本书让我大开眼界",
        "推荐给喜欢科学的朋友",
        "硬核但不晦涩，容易理解"
      )
    );

    Map<String, List<String>> longReviewsByCategory = Map.of(
      "文学小说", List.of(
        "这本书真的改变了我！读完之后思考了很久，很多之前想不通的事情突然豁然开朗。作者用很平实的语言讲了很多深刻的道理，没有华丽的辞藻，但每一句话都很有力量。书中的人物刻画太成功了，每个人都有血有肉，让人印象深刻。强烈推荐给所有喜欢文学的朋友。",
        "从翻开第一页开始就被吸引住了，完全停不下来！故事的设定很新颖，人物塑造也很成功，每个人物都有血有肉。最打动我的是主角面对困境时的坚持和勇气，让我想起了自己曾经的经历，真的很有共鸣。这本书值得反复阅读。",
        "这是一本需要慢慢品读的书。第一遍读可能觉得有点平淡，但越读越有味道。里面有很多值得深思的细节，每次重读都会有新的发现。作者真的很用心，每个情节都安排得很巧妙，伏笔也都回收得很好。强烈推荐！",
        "深夜读完这本书，心情久久不能平静。故事很感人，但不是那种刻意煽情的感动，而是发自内心的触动。里面关于人生、关于选择的思考让我想了很多，可能这就是好书的魅力吧，不仅讲故事，还能引发思考。",
        "花了一个周末把这本书看完了，真的太精彩了！情节环环相扣，高潮迭起，完全猜不到下一步会发生什么。最难得的是，在紧张的剧情中还融入了很多温暖的情感，让人在紧张之余也能感受到人性的美好。"
      ),
      "历史传记", List.of(
        "读完这本书，对这段历史有了全新的认识。作者用通俗的语言把复杂的历史事件讲得很清楚，让我这个历史门外汉也能看懂。书中引用了很多史料，分析也很透彻，让我看到了很多历史人物不为人知的一面。强烈推荐给所有对历史感兴趣的朋友。",
        "原来历史可以这么有趣！作者用生动的笔触描绘了一个个鲜活的历史人物，让枯燥的历史变得生动起来。从书中不仅学到了历史知识，还学到了很多做人做事的道理。以史为鉴，可以知兴替，这句话说得真没错。",
        "这本书让我爱上了历史！作者的研究很深入，每个细节都讲得很清楚。从书中看到了时代的变迁，也看到了人性的复杂。历史不是枯燥的年份数字，而是一个个鲜活的故事，一个个有血有肉的人物。强烈推荐！",
        "作者的观点很新颖，刷新了我对历史的认知。很多事情并不是非黑即白的，历史人物也不是简单的好人或坏人。这本书教会了我从多个角度看问题，而不是被单一的观点所束缚。受益匪浅！",
        "史料详实，分析透彻。作者用严谨的态度对待历史，每个结论都有充分的证据支持。从书中不仅了解了历史事件，更重要的是学会了如何看待历史，如何从历史中吸取教训。这是一本值得反复阅读的好书。"
      ),
      "计算机编程", List.of(
        "这本书让我的编程能力提升了一个档次！作者把复杂的概念讲得通俗易懂，每个例子都很实用。书中的原则和规范让我的代码质量提升了很多，同事都说我的代码变得更优雅了。强烈推荐给每个程序员。",
        "设计模式讲得太透彻了！以前总是听说设计模式，但一直不太理解怎么用。这本书用实际例子讲解每个模式的应用场景，让我豁然开朗。现在写代码的时候会不自觉地用到这些模式，代码质量明显提升。",
        "算法导论，程序员的圣经！这本书虽然有点厚，但内容真的很扎实。每个算法都讲得很清楚，从原理到实现再到复杂度分析，一应俱全。面试前看这本书，帮我搞定了很多算法题。",
        "代码整洁之道，名不虚传！这本书教会了我如何写出易于维护的代码。书中的原则看似简单，但真正做到却不容易。跟着书中的建议做，我的代码变得更加清晰、更加易于理解。强烈推荐！",
        "深入浅出，容易理解。作者用通俗易懂的语言讲解了很多复杂的概念，让我这个初学者也能看懂。书中的例子很实用，可以直接用到项目中。这本书是我读过最好的编程书籍之一。"
      ),
      "心理学", List.of(
        "这本书让我更了解自己了！以前总是被情绪左右，读完这本书后学会了如何管理情绪。书中的方法很实用，可以直接用到生活中。人际关系也变得更和谐了，推荐给所有想要自我成长的朋友。",
        "豁然开朗！很多困惑了我很久的问题都解开了。书中的观点很有启发性，让我重新审视自己的思维方式。学会了非暴力沟通，和家人朋友的关系变得更好了。这本书真的很治愈。",
        "被讨厌的勇气，让我更自信了！以前总是在意别人的看法，活得很累。读完这本书后，学会了课题分离，明白了每个人都有自己的人生课题。现在活得更自在了，推荐给所有被人际关系困扰的朋友。",
        "思考快与慢，了解自己的思维！书中介绍了我们大脑的两套思维系统，让我明白了很多决策背后的原因。学会了在做重要决策时慢下来，避免被直觉误导。这本书改变了我的思维方式。",
        "影响力这本书让我看清了很多！以前总是被各种营销手段影响，读完这本书后学会了识别这些技巧。现在做决策更加理性了，不会轻易被别人影响。强烈推荐！"
      ),
      "经济学", List.of(
        "经济学思维让我看问题更透彻了！以前对很多经济现象都不理解，读完这本书后豁然开朗。作者用通俗的语言讲清楚了经济学原理，让我这个门外汉也能看懂。现在看新闻、做决策都更理性了。",
        "魔鬼经济学，视角独特！作者用经济学的眼光看待各种社会现象，得出了很多出人意料的结论。这本书让我明白，很多事情背后都有经济规律在起作用。推荐给所有对社会现象感兴趣的朋友。",
        "贫穷的本质，引人深思！这本书用大量的实证研究揭示了贫困的根源，让我对贫困问题有了全新的认识。作者提出的解决方案很有启发性，值得我们每个人思考。",
        "国富论，经济学的经典！虽然这本书有点老，但里面的很多思想至今仍然适用。亚当·斯密对市场、分工的论述很深刻，让我明白了很多经济学的基本原理。推荐给所有想了解经济学的人。",
        "薛兆丰的经济学讲义很接地气！作者用很多生活中的例子讲解经济学原理，让枯燥的经济学变得有趣起来。书中的观点很新颖，让我对很多事情有了新的看法。强烈推荐！"
      ),
      "少儿绘本", List.of(
        "亲子共读的好书！每次和孩子一起读这本书，都有不同的收获。孩子很喜欢书中的故事，每次都要读好几遍。画面很精美，色彩鲜艳，很吸引孩子的注意力。推荐给所有有孩子的朋友。",
        "温馨感人！这本书不仅孩子喜欢，大人读了也很感动。故事很简单，但蕴含的道理很深刻。孩子从中学到了很多，比如分享、友谊、勇气等等。这是一本值得收藏的绘本。",
        "启蒙读物的好选择！这本书用简单易懂的方式教孩子认识世界。画面很有艺术感，文字也很优美。孩子从中学到了很多知识，也培养了阅读兴趣。强烈推荐！",
        "睡前故事的最佳选择！这本书的故事很温馨，很适合睡前读。孩子听着故事入睡，睡得特别香。书中的价值观很正，教会了孩子很多做人的道理。",
        "孩子超爱！这本书已经读了无数遍了，但孩子还是百听不厌。画面精美，故事有趣，是一本很棒的绘本。推荐给所有家长。"
      ),
      "科普科幻", List.of(
        "脑洞大开！作者的想象力太丰富了，构建了一个完整的世界观。书中的设定很严谨，科学知识和故事结合得很好。看完这本书，对宇宙、对科学都有了新的认识。推荐给所有科幻迷！",
        "时间简史，经典中的经典！史蒂芬·霍金用通俗的语言讲解了宇宙学的基本概念，让我这个门外汉也能看懂。书中的内容很深刻，但读起来并不枯燥。强烈推荐！",
        "枪炮病菌与钢铁，视角独特！作者从地理、气候等角度解释了人类社会发展的差异，让我对历史有了全新的认识。这本书的观点很有启发性，值得我们每个人思考。",
        "沙丘，科幻史诗！这本书构建了一个庞大的世界观，人物众多但关系清晰。故事很精彩，充满了政治、宗教、生态等元素。看完这本书，对科幻小说有了新的认识。",
        "流浪地球，中国科幻的骄傲！作者的想象力很丰富，故事设定很有创意。书中的家国情怀很感人，让我为中国科幻感到自豪。推荐给所有喜欢科幻的朋友。"
      )
    );

    List<String> defaultShortReviews = List.of(
      "👍 强推！",
      "好看！",
      "太绝了！",
      "值得一读",
      "经典！",
      "五星好评",
      "太棒了",
      "好书！",
      "推荐！",
      "入坑不亏"
    );

    List<String> defaultMediumReviews = List.of(
      "这本书改变了我的很多想法，强烈推荐给大家",
      "读的时候一直在做笔记，满满的干货",
      "作者的想象力太丰富了，脑洞大开",
      "书里的观点很新颖，让我重新思考很多问题",
      "翻译很流畅，读起来一点不费劲",
      "适合慢慢读，边读边思考",
      "这是我今年读过最有收获的书",
      "知识密度很高，需要慢慢消化",
      "节奏把握得很好，不拖沓",
      "观点很犀利，一针见血"
    );

    List<String> defaultLongReviews = List.of(
      "作为一个对这个领域完全不了解的人，这本书真的帮了我很多！作者用通俗易懂的方式把复杂的概念讲清楚，例子也很生动，让我这个门外汉也能看懂。现在已经推荐给好几个朋友了，都说受益匪浅。",
      "这本书我看了三遍，每次都有不同的感受。第一遍看故事，第二遍看人物，第三遍看作者想表达的深层含义。每次重读都能发现新的细节，真的是一本值得反复阅读的好书。",
      "本来只是随手翻开看看，没想到一发不可收拾！作者的文字很有感染力，让我仿佛身临其境。里面的很多观点都很独特，挑战了我的固有认知，让我重新思考很多问题。"
    );

    List<UserAccount> users = userAccountRepository.findAll();
    List<Book> books = bookRepository.findAll();

    for (Book book : books) {
      long existingCount = bookReviewRepository.countByBookCode(book.getCode());
      if (existingCount > 0) {
        continue;
      }
      
      Set<String> usedReviews = new HashSet<>();
      int reviewCount = random.nextInt(15) + 8;
      
      String bookCategory = book.getCategory();
      if (bookCategory == null || bookCategory.isEmpty()) {
        bookCategory = "default";
      }
      
      for (int i = 0; i < reviewCount; i++) {
        boolean forceHighLike = i == reviewCount - 1;
        UserAccount user = users.get(random.nextInt(users.size()));
        
        int reviewType = random.nextInt(10);
        List<String> selectedReviews;
        if (reviewType < 2) {
          selectedReviews = shortReviewsByCategory.getOrDefault(bookCategory, defaultShortReviews);
        } else if (reviewType < 7) {
          selectedReviews = mediumReviewsByCategory.getOrDefault(bookCategory, defaultMediumReviews);
        } else {
          selectedReviews = longReviewsByCategory.getOrDefault(bookCategory, defaultLongReviews);
        }
        
        String content = null;
        int maxAttempts = 50;
        int attempts = 0;
        while (content == null && attempts < maxAttempts) {
          String candidate = selectedReviews.get(random.nextInt(selectedReviews.size()));
          if (!usedReviews.contains(candidate)) {
            content = candidate;
            usedReviews.add(candidate);
          }
          attempts++;
        }
        
        if (content == null) {
          content = selectedReviews.get(random.nextInt(selectedReviews.size()));
        }
        
        double rating = (double) (random.nextInt(5) + 1);

        BookReview review = new BookReview();
        review.setBookCode(book.getCode());
        review.setUserId(user.getId());
        review.setUserName(user.getUsername());
        review.setRating(rating);
        review.setContent(content);
        review.setCreatedAt(Instant.now().minusSeconds(random.nextLong(60 * 24 * 60 * 60)));

        BookReview savedReview = bookReviewRepository.save(review);
        
        int maxLikes = users.size() - 1;
        int likeCount;
        if (forceHighLike) {
          double rand = random.nextDouble();
          if (rand < 0.5) {
            likeCount = Math.min(random.nextInt(30) + 20, maxLikes);
          } else if (rand < 0.8) {
            likeCount = Math.min(random.nextInt(50) + 50, maxLikes);
          } else {
            likeCount = Math.min(random.nextInt(100) + 100, maxLikes);
          }
        } else {
          likeCount = Math.min(generateRealisticLikeCount(), maxLikes);
        }
        Set<Long> usedLikers = new HashSet<>();
        usedLikers.add(user.getId());
        List<BookReviewLike> likes = new ArrayList<>();
        
        for (int j = 0; j < likeCount; j++) {
          UserAccount liker;
          int likerAttempts = 0;
          do {
            liker = users.get(random.nextInt(users.size()));
            likerAttempts++;
          } while (usedLikers.contains(liker.getId()) && likerAttempts < 100);
          
          if (!usedLikers.contains(liker.getId())) {
            usedLikers.add(liker.getId());
            BookReviewLike like = new BookReviewLike();
            like.setReviewId(savedReview.getId());
            like.setUserId(liker.getId());
            likes.add(like);
          }
        }
        if (!likes.isEmpty()) {
          bookReviewLikeRepository.saveAll(likes);
        }
      }
    }
  }

  private int generateRealisticLikeCount() {
    double rand = random.nextDouble();
    if (rand < 0.45) {
      return random.nextInt(5) + 1;
    } else if (rand < 0.75) {
      return random.nextInt(14) + 6;
    } else if (rand < 0.92) {
      return random.nextInt(30) + 20;
    } else if (rand < 0.98) {
      return random.nextInt(50) + 50;
    } else {
      return random.nextInt(100) + 100;
    }
  }

  private void seedBooklists() {
    List<UserAccount> users = userAccountRepository.findAll();
    
    List<String[]> booklistData = List.of(
      new String[]{"bl-001", "我的年度书单", "今年读过的最棒的十本书，每一本都值得推荐！", "lit-001,lit-002,lit-004,lit-007,his-005,psy-001,eco-004,mgt-001,sci-003,mgt-004"},
      new String[]{"bl-002", "程序员必读书单", "从入门到精通，这些书陪伴我成长", "cs-001,cs-002,cs-003,cs-004,cs-005,cs-006,mgt-005"},
      new String[]{"bl-003", "心理学入门推荐", "系统了解心理学的最佳路径", "psy-001,psy-002,psy-003,psy-004,psy-005,psy-006"},
      new String[]{"bl-004", "经典文学之旅", "穿越时空的文学盛宴", "lit-001,lit-003,lit-004,lit-008,lit-009,lit-011,lit-012,lit-013,lit-014,lit-023"},
      new String[]{"bl-005", "经济学入门", "用经济学思维看世界", "eco-001,eco-002,eco-003,eco-004,eco-005"},
      new String[]{"bl-006", "科幻迷必看", "探索宇宙的无限可能", "sci-001,sci-002,sci-003,sci-004,sci-005,lit-007"},
      new String[]{"bl-007", "历史的温度", "从不同角度看历史", "his-001,his-002,his-003,his-004,his-005,his-006,his-007"},
      new String[]{"bl-008", "亲子共读绘本", "和孩子一起阅读的美好时光", "kid-001,kid-002,kid-003,kid-004,kid-005"},
      new String[]{"bl-009", "管理学经典", "提升领导力的必读之作", "mgt-001,mgt-002,mgt-003,mgt-004,mgt-005"},
      new String[]{"bl-010", "中国文学经典", "感受中华文化的博大精深", "lit-001,lit-002,lit-003,lit-011,lit-012,lit-013,lit-015,lit-016"},
      new String[]{"bl-011", "西方文学经典", "品味西方文学的魅力", "lit-004,lit-008,lit-010,lit-014,lit-017,lit-018,lit-019,lit-020,lit-022,lit-023"},
      new String[]{"bl-012", "治愈系书单", "在文字中找到温暖与力量", "lit-009,lit-002,psy-002,kid-001,kid-003"},
      new String[]{"bl-013", "熬夜也想读完的小说", "这些书让我爱不释手", "lit-001,lit-007,lit-004,lit-011,sci-003"},
      new String[]{"bl-014", "提升认知的书单", "打开新世界的大门", "psy-001,eco-001,sci-001,his-001,cs-003"},
      new String[]{"bl-015", "睡前阅读书单", "适合睡前放松阅读的书", "lit-009,kid-001,kid-002,lit-014,lit-015"}
    );

    for (String[] data : booklistData) {
      UserAccount creator = users.get(random.nextInt(users.size()));
      String code = data[0];
      String title = data[1];
      String description = data[2];
      String bookCodes = data[3];
      int bookCount = bookCodes.split(",").length;

      Booklist booklist = booklistRepository.findByCode(code).orElseGet(Booklist::new);
      booklist.setCode(code);
      booklist.setTitle(title);
      booklist.setDescription(description);
      booklist.setCreatorId(creator.getId());
      booklist.setCreatorName(creator.getUsername());
      booklist.setBookCodes(bookCodes);
      booklist.setBookCount(bookCount);
      if (booklist.getId() == null) {
        booklist.setFollowerCount(random.nextInt(300) + 20);
        booklist.setLikeCount(random.nextInt(500) + 30);
        booklist.setRating(Math.round((3.5 + random.nextDouble() * 1.5) * 10) / 10.0);
      }
      booklist.setIsPublic(true);

      booklistRepository.save(booklist);
    }
    seedBooklistLikesAndFollows();
  }

  private void seedBooklistLikesAndFollows() {
    List<UserAccount> users = userAccountRepository.findAll();
    List<Booklist> booklists = booklistRepository.findAll();

    for (Booklist booklist : booklists) {
      int targetLikes = booklist.getLikeCount() != null ? booklist.getLikeCount() : 30;
      int targetFollows = booklist.getFollowerCount() != null ? booklist.getFollowerCount() : 20;

      int existingLikes = (int) likeRepository.countByBooklistCode(booklist.getCode());
      int existingFollows = (int) followRepository.countByBooklistCode(booklist.getCode());

      Set<Long> likedUserIds = new HashSet<>();
      for (int i = 0; i < targetLikes && likedUserIds.size() < users.size(); i++) {
        UserAccount user = users.get(random.nextInt(users.size()));
        if (!likedUserIds.contains(user.getId()) && !likeRepository.existsByBooklistCodeAndUserId(booklist.getCode(), user.getId())) {
          likedUserIds.add(user.getId());
          BooklistLike like = new BooklistLike();
          like.setBooklistCode(booklist.getCode());
          like.setUserId(user.getId());
          like.setUserName(user.getUsername());
          likeRepository.save(like);
        }
      }

      Set<Long> followedUserIds = new HashSet<>();
      for (int i = 0; i < targetFollows && followedUserIds.size() < users.size(); i++) {
        UserAccount user = users.get(random.nextInt(users.size()));
        if (!followedUserIds.contains(user.getId()) && followRepository.findByBooklistCodeAndUserId(booklist.getCode(), user.getId()).isEmpty()) {
          followedUserIds.add(user.getId());
          BooklistFollow follow = new BooklistFollow();
          follow.setBooklistCode(booklist.getCode());
          follow.setUserId(user.getId());
          followRepository.save(follow);
        }
      }
    }
  }

  private void seedActivities() {
    List<UserAccount> users = userAccountRepository.findAll();
    LocalDate today = LocalDate.now();

    String[][] activityData = new String[][] {
      new String[]{"act-001", "春日读书分享会", "在春暖花开的季节，与书友们一起分享阅读心得",
        today.plusDays(1).toString(), today.plusDays(1).toString(), "城市图书馆三楼报告厅", "线下", "upcoming"},
      new String[]{"act-002", "科幻小说读书会", "一起探讨《三体》中的宇宙观与哲学思考",
        today.toString(), today.toString(), "科幻咖啡馆", "线下", "ongoing"},
      new String[]{"act-003", "心理学读书沙龙", "解读《被讨厌的勇气》，探索自我成长之路",
        today.plusDays(2).toString(), today.plusDays(2).toString(), "心灵驿站工作室", "线下", "upcoming"},
      new String[]{"act-004", "经典文学线上共读", "每周六晚线上共读经典文学作品",
        today.minusDays(7).toString(), today.plusMonths(1).toString(), "腾讯会议", "线上", "ongoing"},
      new String[]{"act-005", "程序员读书夜", "技术书籍分享与代码交流",
        today.plusDays(3).toString(), today.plusDays(3).toString(), "创新科技园A座", "线下", "upcoming"},
      new String[]{"act-006", "亲子绘本故事会", "为小朋友们带来精彩的绘本故事",
        today.plusDays(4).toString(), today.plusDays(4).toString(), "青少年活动中心", "线下", "upcoming"},
      new String[]{"act-007", "经济学讲座", "《贫穷的本质》作者见面会",
        today.plusDays(7).toString(), today.plusDays(7).toString(), "大学学术报告厅", "线下", "upcoming"},
      new String[]{"act-008", "历史爱好者聚会", "聊聊《万历十五年》背后的历史",
        today.plusDays(10).toString(), today.plusDays(10).toString(), "历史博物馆会议室", "线下", "upcoming"},
      new String[]{"act-009", "《活着》读后感分享会", "一起聊聊余华的经典作品带给我们的感动",
        today.minusDays(1).toString(), today.minusDays(1).toString(), "时光书店", "线下", "ended"},
      new String[]{"act-010", "《红楼梦》主题读书会", "探讨红楼梦中的人物与命运",
        today.plusDays(5).toString(), today.plusDays(5).toString(), "大观园内", "线下", "upcoming"},
      new String[]{"act-011", "创业者读书会", "分享创业路上的感悟与收获",
        today.toString(), today.plusDays(1).toString(), "创业咖啡", "线下", "ongoing"},
      new String[]{"act-012", "儿童阅读工作坊", "指导家长如何培养孩子的阅读习惯",
        today.plusDays(6).toString(), today.plusDays(6).toString(), "市图书馆培训室", "线下", "upcoming"},
      new String[]{"act-013", "诗歌朗诵会", "在春天里读诗，感受文字的韵律美",
        today.plusDays(8).toString(), today.plusDays(8).toString(), "城市公园露天剧场", "线下", "upcoming"},
      new String[]{"act-014", "职场人读书会", "平衡工作与生活，提升自我",
        today.plusDays(12).toString(), today.plusDays(12).toString(), "城市文化中心", "线下", "upcoming"},
      new String[]{"act-015", "推理小说之夜", "一起解谜，共度悬疑之夜",
        today.plusDays(14).toString(), today.plusDays(14).toString(), "推理主题书店", "线下", "upcoming"},
      new String[]{"act-016", "线上写作分享会", "从阅读到写作，分享创作心得",
        today.plusDays(3).toString(), today.plusDays(3).toString(), "Zoom会议", "线上", "upcoming"},
      new String[]{"act-017", "古籍鉴赏会", "线装书与古籍版本鉴赏",
        today.plusDays(11).toString(), today.plusDays(11).toString(), "市古籍图书馆", "线下", "upcoming"},
      new String[]{"act-018", "读书漂流活动", "分享好书，传递知识",
        today.minusDays(3).toString(), today.plusMonths(1).toString(), "全城各书店", "线下", "ongoing"}
    };

    for (String[] data : activityData) {
      UserAccount creator = users.get(random.nextInt(users.size()));
      String code = data[0];
      String title = data[1];
      String description = data[2];
      String startDate = data[3];
      String endDate = data[4];
      String location = data[5];
      String type = data[6];
      String status = data[7];

      Activity activity = activityRepository.findByCode(code).orElseGet(Activity::new);
      activity.setCode(code);
      activity.setTitle(title);
      activity.setDescription(description);
      activity.setOrganizerId(creator.getId());
      activity.setOrganizerName(creator.getUsername());
      activity.setStartDate(startDate);
      activity.setEndDate(endDate);
      activity.setLocation(location);
      activity.setActivityType(type);
      activity.setStatus(status);
      activity.setApprovalStatus("approved");
      activity.setParticipantCount(random.nextInt(40) + 5);
      activity.setMaxParticipants(100);
      activity.setCoverUrl("https://picsum.photos/seed/" + code + "/400/200");

      activityRepository.save(activity);
    }
  }

  private void seedBooklistComments() {
    List<String> comments = List.of(
      "收藏了，书单很棒！",
      "书单很用心，已经读了其中两本",
      "感谢分享，正愁不知道读什么呢！",
      "书单质量很高，强烈推荐",
      "有几本已经读过，确实不错",
      "期待后续更新！",
      "这个书单太对我胃口了！",
      "已经Mark，慢慢读",
      "书单结构很好，分类清晰",
      "感谢推荐，受益匪浅",
      "这个书单太棒了，每一本都是经典",
      "感谢作者的整理！",
      "收藏了，慢慢实践",
      "很多想看的书都在里面",
      "这个书单太合我心意了！",
      "收藏收藏，都是好书",
      "太棒了，感谢分享！",
      "这个书单我要好好看看",
      "都是值得读的好书",
      "感谢这么棒的书单"
    );

    List<UserAccount> users = userAccountRepository.findAll();
    List<Booklist> booklists = booklistRepository.findAll();

    for (Booklist booklist : booklists) {
      long existingCount = booklistCommentRepository.countByBooklistCode(booklist.getCode());
      if (existingCount > 0) {
        continue;
      }
      
      int commentCount = random.nextInt(6) + 2;
      for (int i = 0; i < commentCount; i++) {
        UserAccount user = users.get(random.nextInt(users.size()));
        String content = comments.get(random.nextInt(comments.size()));

        BooklistComment comment = new BooklistComment();
        comment.setBooklistCode(booklist.getCode());
        comment.setUserId(user.getId());
        comment.setUsername(user.getUsername());
        comment.setContent(content);
        comment.setCreatedAt(Instant.now().minusSeconds(random.nextLong(15 * 24 * 60 * 60)));

        booklistCommentRepository.save(comment);
      }
    }
  }

  private void seedBasicUsers() {
    if (userAccountRepository.count() > 0) {
      return;
    }
    
    List<UserAccount> users = new ArrayList<>();
    users.add(createUser("zhangsan", "zhangsan@example.com", "张三", "热爱阅读的程序员", "13800138001"));
    users.add(createUser("lisi", "lisi@example.com", "李四", "书评人", "13800138002"));
    users.add(createUser("wangwu", "wangwu@example.com", "王五", "图书馆管理员", "13800138003"));
    users.add(createUser("admin", "admin@shuyou.com", "系统管理员", "管理员账号", "13800138999", "ADMIN"));

    for (UserAccount user : users) {
      user.setPasswordHash(passwordEncoder.encode(user.getEmail().equals("admin@shuyou.com") ? "admin123456" : "123456"));
      user.setStatus(1);
      user.setAvatarUrl("https://picsum.photos/seed/" + user.getUsername() + "/100/100");
      userAccountRepository.save(user);
    }
  }

  private void seedBasicBooks() {
    if (bookRepository.count() > 0) {
      return;
    }

    List<Book> books = new ArrayList<>(List.of(
      seedBook("lit-001", "红楼梦", "曹雪芹", "文学小说", 4.9, 1280, true, "红楼梦.png", "中国古典长篇小说巅峰之作", "1996-12", "人民文学出版社", 1600, "9787020002207"),
      seedBook("lit-002", "活着", "余华", "文学小说", 4.8, 1100, true, "活着.png", "通过福贵一生的遭际", "2012-08", "作家出版社", 191, "9787506365437"),
      seedBook("lit-007", "三体", "刘慈欣", "文学小说", 4.9, 2380, true, "https://picsum.photos/seed/santi/200/300", "地球文明与三体文明的史诗级碰撞", "2008-01", "重庆出版社", 302, "9787536692930"),
      seedBook("lit-011", "西游记", "吴承恩", "文学小说", 4.9, 2100, true, "西游记.jpg", "中国古典四大名著之一", "1995-01", "人民文学出版社", 890, "9787020001234"),
      seedBook("lit-012", "三国演义", "罗贯中", "文学小说", 4.9, 1950, true, "三国演义.jpg", "中国古典四大名著之一", "1995-03", "人民文学出版社", 980, "9787020001241"),
      seedBook("lit-013", "水浒传", "施耐庵", "文学小说", 4.8, 1890, true, "水浒传.jpg", "中国古典四大名著之一", "1995-02", "人民文学出版社", 930, "9787020001258")
    ));

    for (Book book : books) {
      bookRepository.save(book);
    }
  }

  private void seedBasicBooklists() {
    if (booklistRepository.count() > 0) {
      return;
    }

    List<UserAccount> users = userAccountRepository.findAll();
    if (users.isEmpty()) {
      return;
    }

    Booklist booklist = new Booklist();
    booklist.setCode("bl-001");
    booklist.setTitle("我的书单");
    booklist.setDescription("精选好书推荐");
    booklist.setCreatorId(users.get(0).getId());
    booklist.setCreatorName(users.get(0).getUsername());
    booklist.setBookCodes("lit-001,lit-002,lit-007");
    booklist.setBookCount(3);
    booklist.setFollowerCount(10);
    booklist.setLikeCount(20);
    booklist.setRating(4.5);
    booklist.setIsPublic(true);
    booklistRepository.save(booklist);
  }

  private void seedBasicActivities() {
    if (activityRepository.count() > 0) {
      return;
    }

    List<UserAccount> users = userAccountRepository.findAll();
    if (users.isEmpty()) {
      return;
    }

    LocalDate today = LocalDate.now();

    Activity activity = new Activity();
    activity.setCode("act-001");
    activity.setTitle("读书分享会");
    activity.setDescription("与书友们一起分享阅读心得");
    activity.setOrganizerId(users.get(0).getId());
    activity.setOrganizerName(users.get(0).getUsername());
    activity.setStartDate(today.plusDays(1).toString());
    activity.setEndDate(today.plusDays(1).toString());
    activity.setLocation("城市图书馆");
    activity.setActivityType("线下");
    activity.setStatus("upcoming");
    activity.setApprovalStatus("approved");
    activity.setParticipantCount(15);
    activity.setMaxParticipants(50);
    activity.setCoverUrl("https://picsum.photos/seed/act-001/400/200");
    activityRepository.save(activity);
  }
}
