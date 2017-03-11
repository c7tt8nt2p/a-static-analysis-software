using System;
namespace TestApplication2{

	class Test{
		bool isInitialized;
		void Main(){
			int x = 0;
			if (x > 5) {
				Console.WriteLine("OK");
			} else if (x > 8) {
				Console.WriteLine("NOT OK");
			} else {
				Console.WriteLine("NOT NOT OK");
			}
            Console.WriteLine("END");
			Console.ReadLine();
		}
	}
}
