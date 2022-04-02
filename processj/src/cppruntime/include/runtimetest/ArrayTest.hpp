/*!
 * ProcessJTest::ArrayTest Declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_ARRAY_TEST_HPP
#define UNLV_PROCESS_J_ARRAY_TEST_HPP

namespace ProcessJTest { class ArrayTest; }

static void println(){

    std::cout << std::endl;
}

template<typename T>
static void println(T arg) {

    std::cout << arg << std::endl;

}

template<typename T, typename...Ts>
static void println(T arg, Ts... args) {

    std::cout << arg;
    println(args...);

}

template<typename T>
static void print(T arg) {

    std::cout << arg;

}

template<typename T, typename... Ts>
static void print(T arg, Ts... args) {

    std::cout << arg;
    print(args...);

}

class ProcessJTest::ArrayTest {

    /// --------------
    /// Public Members

public:
        ArrayTest() {

            std::cout << "instantiating test..." << std::endl;

        }

        void run() {

            ProcessJRuntime::Array<ProcessJRuntime::Integer32>* pj_arr = new ProcessJRuntime::Array<ProcessJRuntime::Integer32>({1, 2, 3, 4});

            std::cout << *(pj_arr);

            delete pj_arr;

            ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>* pj_md_arr_b =
                new ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>{new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{1, 2, 3, 4},
                                                                             new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{5, 6, 7, 8},
                                                                             new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{9, 10, 11, 12},
                                                                             new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{13, 14, 15, 16}};

            std::cout << *(pj_md_arr_b);

            delete pj_md_arr_b;

            ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>*>* pj_md_arr_c =
            new ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>*>{new ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>{new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{1, 2, 3, 4},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{5, 6, 7, 8},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{9, 10, 11, 12},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{13, 14, 15, 16}},
                                                             new ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>{new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{17, 18, 19, 20},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{21, 22, 23, 24},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{25, 26, 27, 28},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{29, 30, 31, 32}},
                                                             new ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>{new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{33, 34, 35, 36},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{37, 38, 39, 40},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{41, 42, 43, 44},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{45, 46, 47, 48}},
                                                             new ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>{new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{49, 50, 51, 52},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{53, 54, 55, 56},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{57, 58, 59, 60},
                                                                                                 new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{61, 62, 63, 64}}};

            std::cout << *(pj_md_arr_c);

            delete pj_md_arr_c;
        }
};


#endif
