export const fetchLogout = async () => {
    const refresh = localStorage.getItem("refresh");
    const endpoint = "/api/user/logout";
    try {
      await fetch(endpoint, {
        method: "POST",
        headers: {
          "refresh": refresh,
        },
      });
    } catch (error) {
      console.error("Error: ", error);
    } finally {
      localStorage.clear();
    }
}