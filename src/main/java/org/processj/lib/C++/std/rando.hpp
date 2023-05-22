#ifndef PJ_RANDO_HPP
#define PJ_RANDO_HPP

#include<cstdlib>

namespace rando
{

static void initRandom(long seed)
{
    std::srand(static_cast<unsigned>(seed));
}

static long longRandom()
{
    return static_cast<long>(std::rand());
}

}

#endif
