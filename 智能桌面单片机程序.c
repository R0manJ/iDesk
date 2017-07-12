//智能桌面
//超声波测距
//5个LED灯的控制
//台灯控制,通过继电器
#include<reg51.h>
#define uint unsigned int
#define uchar unsigned char
#define DATA_MAX_LENGTH 20 
//管脚定义

//照明区

sbit LED1 = P1^0;
sbit LED2 = P1^1;
//-----
sbit LED3 = P1^2;
sbit LED4 = P1^3;
sbit LED5 = P1^4;

//控制区
//--relay
sbit RELAY = P0^0;
sbit ULTRASONIC_Trigger = P0^1;
sbit ULTRASONIC_ECHO = P0^2;
sbit NE555_4 = P0^3;

//第二功能区
sbit Receive = P3^0;
sbit Transimit = P3^1;

//变量定义区
uint seatIsSitted; //0为没有人;1为有人;
uint isOpenRelay; //0为关闭,1为打开;
uint delayScan; //用于改变扫描间隔 
//如果有人坐在桌前,就将定时器的初值定位50000us(50ms),20个单位为1秒.
//10分钟扫描一次,即是600秒,需要12 000 个单位

//用于记录超声波数据回传的时间
uint recordTH0;
uint recordTL0;
uint recordTotal;
//超声波测量的距离(cm)
uint distance;

//蓝牙收发用的变量
uchar ReceiveCache[DATA_MAX_LENGTH];
uchar TrasimitCache[DATA_MAX_LENGTH];
uint recordIndex = 0;
uint recordDataLength;
//接收最大长度为20, 19位数据位 + 1位结束标识位('#')


//函数声明区
void init();
void initTimer0();
void initTimer1();
void handlerEchoData();
void SendOneByte();
void handlerCommand();
//主程序

void main()
{
    init();
    initTimer0();
    initTimer1();
    //这里要添加一个无限循环函数,否则main函数会被重复执行
    while(1);
}

//初始化总函数
void init()
{
    seatIsSitted = 0;
    isOpenRelay = 0;
    delayScan = 1;

}
//定时器0初始化函数 -- 用于超声波测距
void initTimer0()
{
    TMOD = 0x21; //共同设置定时器1与定时器0的工作方式.
    //发送10us的高电平才能触发超声波模块
    TH0 = 0xff;  
    TL0 = 0xe9;
    EA = 1;
    ES = 1;
    ET0 = 1;
    TR0 = 0;//暂时关闭计时器
    ULTRASONIC_Trigger = 1;
    ULTRASONIC_ECHO = 0;
}

void Timer0Interrupt(void) interrupt 1
{
    
    if(seatIsSitted == 0)
    {
        ULTRASONIC_Trigger = 0;
        TH0 = 0x0FF;
        TL0 = 0x0e9;
        //处理ECHO引脚返回的数据函数
        handlerEchoData();
    }
    else
    {
        //有人的情况下.改变延时时间
        TH0 = 0x4C;
        TL0 = 0x00;
        //依然有人的情况下
        delayScan ++;
        //当delayScan为12 000 的时候,启动扫描
        if(delayScan == 1200 )
        {
            ULTRASONIC_Trigger = 0;
            //处理ECHO引脚返回的数据函数        
            handlerEchoData();    
        }
    }
    //add your code here!
}

//超声波检测函数

void handlerEchoData()
{
    while(ULTRASONIC_ECHO == 0);
    //开始从ECHO口接收数据
    TH0 = 0;
    TL0 = 0;
    while(ULTRASONIC_ECHO == 1);
    //取超声波返回数据的时间
    recordTH0 = TH0;
    recordTL0 = TL0;
    //计时器总时间
    recordTotal = recordTH0 * 256 + recordTL0;  //单位us;
    distance = recordTotal * 0.017;             //单位cm;
    if(distance < 80)
    {
        //如果距离小于70cm,则打开台灯
        //isOpenRelay = 1;
        seatIsSitted = 0;
        RELAY = 0;
        LED1 = 1;
        LED5 = 0;
    }
    else
    {
        RELAY = 1;
        LED1 = 0;
        LED5 = 1;
    }
    ULTRASONIC_Trigger = 1;
    TH0 = 0xff;
    TL0 = 0xE9;
    
}

//定时器1初始化函数 -- 用于蓝牙通讯
void initTimer1()
{
    SCON = 0x50;
    TH1 = 0xFD;
    TL1 = TH1;
    PCON = 0x00; 
    TR1 = 1;
}
 
//蓝牙通讯发送信息函数
void SendOneByte(unsigned char c)
{
    SBUF = c;
    while(!TI);
    TI = 0;
}
//测试函数
void Send128()
{
    SBUF = 128;//C
    while(!TI);
    TI = 0;
}

//接收函数

void UARTInterrupt(void) interrupt 4
{
    uchar temp;
    if(RI)
    {
        RI = 0;
        temp = SBUF;
        if(temp != '#')
        {
            //如果不是结束位,则将接收到的字节保存到缓存数组中
            if(temp != 0x0d)//这里有一个归位值,每次发送完数据后都会附加发送这个值
			{
				ReceiveCache[recordIndex] = temp;
            	recordIndex ++;
        	}
        }
        else{
            //如果为结束位,则保存该数组长度,将记录用的下标清零
            recordDataLength = recordIndex - 1; // 去除'#'
            recordIndex = 0;
            handlerCommand();
        }
        
         //add your code here!
    }
    else
    {
        TI = 0;
    }
}

//处理接收的数据
void handlerCommand(){
    //控制五个灯 
        //1,2,3,4,5
        //呼吸灯状态 ,即是NE555的状态,    1(开)   ,       0(关)
    //一个继电器
        //6
    //开关状态用 1,0表示
        if (ReceiveCache[2] == '1')
        {
            NE555_4 = 1;
        }
        else
        {
            NE555_4 = 0;
        }
        switch(ReceiveCache[0])
        {
            
            case '1':
                if(ReceiveCache[1] == '1') 
                {
                    LED1 = 0;
                }
                else
                {
                    LED1 = 1;
                }
                break;
            case '2':
                if(ReceiveCache[1] == '1') 
                {
                    LED2 = 0;
                }
                else
                {
                    LED2 = 1;
                }
                break;
            case '3':
                if(ReceiveCache[1] == '1') 
                {
                    LED3 = 0;
                }
                else
                {
                    LED3 = 1;
                }
                break;
            case '4':
                if(ReceiveCache[1] == '1') 
                {
                    LED4 = 0;
                }
                else
                {
                    LED4 = 1;
                }
                break;
            case '5':
                if(ReceiveCache[1] == '1') 
                {
                    LED5 = 0;
                }
                else
                {
                    LED5 = 1;
                }
                break;
            case '6':
                if(ReceiveCache[1] == '1') 
                {
                    RELAY = 1;
                }
                else
                {
                    RELAY = 0;
                }
                break;
        }

        if(ReceiveCache[2] == '1')
        {
            //第三位数据用于指示是否开启NE555震荡器.
            //NE555的第四引脚,接通低电平会使NE555不工作
            NE555_4 = 1;
        }
        else
        {
            NE555_4 = 0;
        }
}
 