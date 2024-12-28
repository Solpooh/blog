import {useEffect, useState} from 'react';

const usePagination = <T> (countPerPage: number) => {
    //  state: 전체 객체 리스트 상태 (총 게시물) //
    const [totalList, setTotalList] = useState<T[]>([]);
    //  state: 보여줄 객체 리스트 상태 (한 페이지에 보여줄 게시물 수) //
    const [viewList, setViewList] = useState<T[]>([]);
    //  state: 현재 페이지 번호 상태 (page) //
    const [currentPage, setCurrentPage] = useState<number>(1);

    //  state: 전체 페이지 번호 리스트 상태 [1, 2, ... 35] //
    const [totalPageList, setTotalPageList] = useState<number[]>([1]);
    //  state: 보여줄 페이지 번호 리스트 상태 [1, 2, 3, ... 10] //
    const [viewPageList, setViewPageList] = useState<number[]>([1]);
    //  state: 현재 섹션 상태 [11, 12, 13 ... 20] == 2 //
    const [currentSection, setCurrentSection] = useState<number>(1);

    //  state: 마지막 섹션 상태 [31, 32 ... 35] //
    const [totalSection, setTotalSection] = useState<number>(1);

    //  function: 보여줄 객체 리스트 추출 함수 //
    const setView = () => {
        const FIRST_INDEX = countPerPage * (currentPage - 1);  // 0
        const LAST_INDEX = totalList.length > countPerPage * currentPage ? countPerPage * currentPage : totalList.length; // 3
        const viewList = totalList.slice(FIRST_INDEX, LAST_INDEX); // 0 1 2
        setViewList(viewList);
    };
    //  function: 보여줄 페이지 리스트 추출 함수 //
    const setViewPage = () => {
        const FIRST_INDEX = 10 * (currentSection - 1);
        const LAST_INDEX = totalPageList.length > 10 * currentSection ? 10 * currentSection : totalPageList.length;
        const viewPageList = totalPageList.slice(FIRST_INDEX, LAST_INDEX);
        setViewPageList(viewPageList);
    };

    useEffect(() => {
        const totalPage = Math.ceil(totalList.length / countPerPage);
        const totalPageList = Array.from({ length: totalPage }, (_, i) => i + 1);
        const totalSection = Math.ceil(totalPage / 10);

        setTotalPageList(totalPageList);
        setTotalSection(totalSection);
        setCurrentPage(1);
        setCurrentSection(1);
        setViewList(totalList.slice(0, countPerPage));
        setViewPageList(totalPageList.slice(0, Math.min(10, totalPageList.length)));
    }, [totalList]);

    //  effect: current page 가 변경될 때마다 실행할 작업 //
    useEffect(setView, [currentPage]);

    //  effect: current section 이 변경될 때마다 실행할 작업 //
    useEffect(setViewPage, [currentSection]);

    return {
        currentPage,
        setCurrentPage,
        currentSection,
        setCurrentSection,
        viewList,
        viewPageList,
        totalSection,
        setTotalList
    }
};
export default usePagination;