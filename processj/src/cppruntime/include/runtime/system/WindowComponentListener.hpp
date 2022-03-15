/*!
 * \brief Class that represents a Window Component Callback receiver
 *
 * A basic class that represents a Window Component Callback receiver.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_WINDOW_COMPONENT_LISTENER_HPP
#define UNLV_PROCESS_J_WINDOW_COMPONENT_LISTENER_HPP

namespace ProcessJRuntime{ class WindowComponentListener; }

class ProcessJRuntime::WindowComponentListener {

    /// ---------------
    /// Public Members

public:

    /*!
     * Invoked when the window component is dirty and needs to be drawn.
     *
     * \param component The Component to be drawn as a void pointer
     */

   virtual void OnComponentDirty(void*) = 0;


};

#endif
