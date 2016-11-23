/*  main.c  - main */

#include <xinu.h>
#include <string.h>
int producer_id, consumer_id1, consumer_id2, consumer_id3;
process producer(void){
	int i=0; 
	//kprintf("inside producer\n");
	while(i<100){
 	if(i%10==0){
	  publish(0x0001,0);
	  if(i>0 && i%10==0)
	  sleep(1);
	}
	else if(i%2==0)
	  {publish(0x0101, 2);}		
	else if(i%2==1){
	  publish(0x0201, 1);
	}
	//kprintf("value of i is :%d",i);
	i++;
	}
	kprintf("end of publishing data");	
}
void handle(topic16 tp, uint32 data){
	kprintf("handle invoked with data: %u \n", data);
}
void foo(topic16 tp, uint32 data){
	kprintf("foo invoked with data: %u \n", data);
}

process consumer1(void){
	//kprintf("inside consumer1\n");
	subscribe(0x0101, &handle);
	sleep(1);
	unsubscribe(0x0101);
	//kprintf("consumer1 unsubscribes\n");
} 
process consumer2(void){
	sleep(1);	
	//kprintf("inside consumer2\n");
	subscribe(0x0201, &foo);
	sleep(1);
	//sleep(1);
} 

process	main(void)
{
	recvclr();
	int i=0;
	kprintf("led is about to get turned on\n");	
	for(i=0; i < 8; i++){
	gpioLEDOn(i);
	}
	kprintf("done!\n");
	/**kprintf("\nevent handling yet to start\n");
	consumer_id1 = create(consumer1, 4096, 50, "consumer1",0);
	consumer_id2 = create(consumer2, 4096, 50, "consumer2",0);	
	producer_id  = create(producer, 4096, 50, "producer",0);
	
	resched_cntl(DEFER_START);
	resume(consumer_id1);
	resume(consumer_id2);
	resume(producer_id);
	resched_cntl(DEFER_STOP);
	//kprintf("\nmessages received\n");**/
	return OK;
}


