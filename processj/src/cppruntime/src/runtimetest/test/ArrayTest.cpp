/*!
 * ProcessJTest::ArrayTest implementation. Implements the
 * ProcessJTest::ArrayTest logic
 *
 * \author Carlos L. Cuenca
 * \date 04/06/2022
 * \version 1.1.0
 */

#include<ProcessJRuntimeTest.hpp>

/*!
 * Returns a new instance of ProcessJTest::TextComponent
 *
 * \return ProcessJTest::WindowComponent pointer.
 */

ProcessJTest::WindowComponent* ProcessJTest::ArrayTest::getCreatedWindowComponent() const {

    return new ProcessJTest::TextComponent(0);

}

/*!
 * Contains the ProcessJTest::ArrayTest logic.
 */

void ProcessJTest::ArrayTest::run() {

    ProcessJSystem::TextComponent* windowComponent = dynamic_cast<ProcessJTest::TextComponent*>(getWindowComponent());

    windowComponent->setText("Testing ProcessJRuntime::Array");

    ProcessJRuntime::Array<ProcessJRuntime::Integer32>* pj_arr = new ProcessJRuntime::Array<ProcessJRuntime::Integer32>({1, 2, 3, 4});

    delete pj_arr;

    ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>* pj_md_arr_b =
        new ProcessJRuntime::Array<ProcessJRuntime::Array<ProcessJRuntime::Integer32>*>{new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{1, 2, 3, 4},
                                                                             new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{5, 6, 7, 8},
                                                                             new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{9, 10, 11, 12},
                                                                             new ProcessJRuntime::Array<ProcessJRuntime::Integer32>{13, 14, 15, 16}};

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

            delete pj_md_arr_c;

}
