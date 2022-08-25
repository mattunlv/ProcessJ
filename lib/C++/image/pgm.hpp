#ifndef PJ_PGM_HPP
#define PJ_PGM_HPP

#include <iostream>

class pgm
{
public:

    static void
    writePGM(pj_runtime::pj_md_array<pj_runtime::pj_array<int>*>* pic,
             std::string                                     filename,
             int                                                  max)
    {
        int height = pic->length;
        int width = (*pic)[0]->length;
        FILE* f = fopen(filename.c_str(), "wb");
        fprintf(f,
                "P6\n%d %d\n255\n",
                width,
                height);

        int i, j, k = 0;
        unsigned char* out_ptr =
            reinterpret_cast<unsigned char*>(malloc(sizeof(unsigned char) * width * height * 3));

        for(i = 0; i < height; ++i)
        {
            for(j = 0; j < width; ++j)
            {
                out_ptr[k++] = (*(*pic)[i])[j];
                out_ptr[k++] = (*(*pic)[i])[j];
                out_ptr[k++] = (*(*pic)[i])[j];
            }
        }

        fwrite(out_ptr, sizeof(unsigned char), width * height * 3, f);
        fclose(f);
        free(out_ptr);
    }
};

#endif
