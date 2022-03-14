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

            ProcessJRuntime::Array<int32_t>* pj_arr = new ProcessJRuntime::Array<int32_t>({1, 2, 3, 4});

            int32_t i, j, k;
            for(i = 0; i < pj_arr->size(); ++i) {
                print((*pj_arr)[i], ", ");
            }
            println();
            delete pj_arr;

            ProcessJRuntime::Array<ProcessJRuntime::Array<int32_t>*>* pj_md_arr_b =
                new ProcessJRuntime::Array<ProcessJRuntime::Array<int32_t>*>{new ProcessJRuntime::Array<int32_t>{1, 2, 3, 4},
                                                                            new ProcessJRuntime::Array<int32_t>{5, 6, 7, 8},
                                                                            new ProcessJRuntime::Array<int32_t>{9, 10, 11, 12},
                                                                            new ProcessJRuntime::Array<int32_t>{13, 14, 15, 16}};

            for(i = 0; i < pj_md_arr_b->size(); ++i)
            {
                for(j = 0; j < (*pj_md_arr_b)[i]->size(); ++j)
                {
                    print((*(*pj_md_arr_b)[i])[j], ", ");
                }
            }
            println();
            delete pj_md_arr_b;

            ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Array<int32_t>*>*>* pj_md_arr_c =
            new ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Array<int32_t>*>*>{new ProcessJRuntime::Array<ProcessJRuntime::Array<int32_t>*>{new ProcessJRuntime::Array<int32_t>{1, 2, 3, 4},
                                                                                                 new ProcessJRuntime::Array<int32_t>{5, 6, 7, 8},
                                                                                                 new ProcessJRuntime::Array<int32_t>{9, 10, 11, 12},
                                                                                                 new ProcessJRuntime::Array<int32_t>{13, 14, 15, 16}},
                                                             new ProcessJRuntime::Array<ProcessJRuntime::Array<int32_t>*>{new ProcessJRuntime::Array<int32_t>{17, 18, 19, 20},
                                                                                                 new ProcessJRuntime::Array<int32_t>{21, 22, 23, 24},
                                                                                                 new ProcessJRuntime::Array<int32_t>{25, 26, 27, 28},
                                                                                                 new ProcessJRuntime::Array<int32_t>{29, 30, 31, 32}},
                                                             new ProcessJRuntime::Array<ProcessJRuntime::Array<int32_t>*>{new ProcessJRuntime::Array<int32_t>{33, 34, 35, 36},
                                                                                                 new ProcessJRuntime::Array<int32_t>{37, 38, 39, 40},
                                                                                                 new ProcessJRuntime::Array<int32_t>{41, 42, 43, 44},
                                                                                                 new ProcessJRuntime::Array<int32_t>{45, 46, 47, 48}},
                                                             new ProcessJRuntime::Array<ProcessJRuntime::Array<int32_t>*>{new ProcessJRuntime::Array<int32_t>{49, 50, 51, 52},
                                                                                                 new ProcessJRuntime::Array<int32_t>{53, 54, 55, 56},
                                                                                                 new ProcessJRuntime::Array<int32_t>{57, 58, 59, 60},
                                                                                                 new ProcessJRuntime::Array<int32_t>{61, 62, 63, 64}}};

            for(i = 0; i < pj_md_arr_c->size(); ++i)
            {
                for(j = 0; j < (*pj_md_arr_c)[i]->size(); ++j)
                {
                    for(k = 0; k < (*(*pj_md_arr_c)[i])[j]->size(); ++k)
                    {
                        print((*(*(*pj_md_arr_c)[i])[j])[k], ", ");
                    }
                }
            }
            println();
            delete pj_md_arr_c;
        }
};


#endif
